package com.paklog.lastmile.domain.aggregate;

import com.paklog.lastmile.domain.event.*;
import com.paklog.lastmile.domain.valueobject.GPSCoordinates;
import com.paklog.lastmile.domain.valueobject.RouteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "delivery_routes")
public class DeliveryRoute {

    @Id
    private String id;

    private String routeNumber;
    private RouteStatus status;

    private String vehicleId;
    private String driverId;

    @Builder.Default
    private List<DeliveryStop> stops = new ArrayList<>();

    private int totalStops;
    private int completedStops;
    private int failedStops;

    private double totalDistanceKm;
    private int estimatedDurationMinutes;

    private Instant plannedStartTime;
    private Instant actualStartTime;
    private Instant estimatedEndTime;
    private Instant actualEndTime;

    private GPSCoordinates startLocation;
    private GPSCoordinates currentLocation;

    private double trafficDelayMinutes;
    private int optimizationScore;

    @Version
    private Long version;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Transient
    @Builder.Default
    private List<DomainEvent> domainEvents = new ArrayList<>();

    // Business logic methods

    public void plan() {
        this.status = RouteStatus.PLANNED;
        this.totalStops = stops.size();
        this.completedStops = 0;
        this.failedStops = 0;

        addDomainEvent(RouteCreatedEvent.builder()
            .routeId(this.id)
            .routeNumber(this.routeNumber)
            .vehicleId(this.vehicleId)
            .driverId(this.driverId)
            .totalStops(this.totalStops)
            .build());
    }

    public void start() {
        if (this.status != RouteStatus.PLANNED) {
            throw new IllegalStateException("Route must be in PLANNED status to start");
        }

        this.status = RouteStatus.IN_PROGRESS;
        this.actualStartTime = Instant.now();

        addDomainEvent(DeliveryStartedEvent.builder()
            .routeId(this.id)
            .driverId(this.driverId)
            .vehicleId(this.vehicleId)
            .startedAt(this.actualStartTime)
            .build());
    }

    public void completeStop(String stopId, boolean success) {
        DeliveryStop stop = findStop(stopId);

        if (stop == null) {
            throw new IllegalArgumentException("Stop not found: " + stopId);
        }

        if (success) {
            this.completedStops++;

            addDomainEvent(DeliveryCompletedEvent.builder()
                .routeId(this.id)
                .stopId(stopId)
                .customerId(stop.getCustomerId())
                .completedAt(Instant.now())
                .attemptNumber(stop.getAttemptCount())
                .build());
        } else {
            if (stop.hasFailedMaxAttempts()) {
                this.failedStops++;

                addDomainEvent(DeliveryFailedEvent.builder()
                    .routeId(this.id)
                    .stopId(stopId)
                    .customerId(stop.getCustomerId())
                    .reason(stop.getAttemptResult().name())
                    .attemptsCount(stop.getAttemptCount())
                    .build());
            }
        }

        // Check if route is complete
        if (completedStops + failedStops >= totalStops) {
            complete();
        }
    }

    public void complete() {
        this.status = RouteStatus.COMPLETED;
        this.actualEndTime = Instant.now();

        addDomainEvent(RouteCompletedEvent.builder()
            .routeId(this.id)
            .completedAt(this.actualEndTime)
            .totalStops(this.totalStops)
            .successfulStops(this.completedStops)
            .failedStops(this.failedStops)
            .totalDistanceKm(this.totalDistanceKm)
            .build());
    }

    public void optimize(List<DeliveryStop> optimizedStops, double distanceKm, int score) {
        this.stops = new ArrayList<>(optimizedStops);
        this.totalDistanceKm = distanceKm;
        this.optimizationScore = score;
        this.totalStops = optimizedStops.size();

        // Recalculate estimated duration
        this.estimatedDurationMinutes = calculateEstimatedDuration();
        this.estimatedEndTime = plannedStartTime.plus(estimatedDurationMinutes, ChronoUnit.MINUTES);

        addDomainEvent(RouteOptimizedEvent.builder()
            .routeId(this.id)
            .totalDistance(distanceKm)
            .optimizationScore(score)
            .stopCount(optimizedStops.size())
            .build());
    }

    public void updateTrafficDelay(double delayMinutes) {
        this.trafficDelayMinutes = delayMinutes;

        // Recalculate ETAs for all remaining stops
        recalculateETAs();
    }

    public void addStop(DeliveryStop stop) {
        if (stops.size() >= 50) {
            throw new IllegalStateException("Maximum 50 stops per route");
        }

        stop.setSequence(stops.size() + 1);
        stops.add(stop);
        this.totalStops = stops.size();
    }

    private DeliveryStop findStop(String stopId) {
        return stops.stream()
            .filter(s -> s.getStopId().equals(stopId))
            .findFirst()
            .orElse(null);
    }

    private int calculateEstimatedDuration() {
        // Base calculation: driving time + stop durations
        double drivingMinutes = totalDistanceKm * 2.5; // Assume 24 km/h average urban speed
        int stopDurations = stops.stream()
            .mapToInt(DeliveryStop::getEstimatedDurationMinutes)
            .sum();

        return (int) (drivingMinutes + stopDurations + trafficDelayMinutes);
    }

    private void recalculateETAs() {
        Instant currentETA = actualStartTime != null ? actualStartTime : plannedStartTime;

        for (DeliveryStop stop : stops) {
            if (!stop.isCompleted()) {
                stop.updateETA(currentETA);

                // Add travel time to next stop + stop duration
                if (stop.getEstimatedDurationMinutes() > 0) {
                    currentETA = currentETA.plus(stop.getEstimatedDurationMinutes(), ChronoUnit.MINUTES);
                }
            }
        }

        // Send customer notifications for updated ETAs
        addDomainEvent(CustomerNotifiedEvent.builder()
            .routeId(this.id)
            .notificationType("ETA_UPDATE")
            .message("Your delivery ETA has been updated")
            .build());
    }

    private void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public DeliveryStop getNextStop() {
        return stops.stream()
            .filter(s -> !s.isCompleted())
            .findFirst()
            .orElse(null);
    }

    public int getRemainingStops() {
        return totalStops - completedStops - failedStops;
    }
}
