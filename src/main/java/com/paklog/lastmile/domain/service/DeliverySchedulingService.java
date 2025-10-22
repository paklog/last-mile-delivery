package com.paklog.lastmile.domain.service;

import com.paklog.lastmile.domain.aggregate.DeliveryRoute;
import com.paklog.lastmile.domain.aggregate.DeliveryStop;
import com.paklog.lastmile.domain.aggregate.Vehicle;
import com.paklog.lastmile.domain.valueobject.DeliveryWindow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles delivery scheduling and time window management
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliverySchedulingService {

    private final RouteOptimizationService optimizationService;

    /**
     * Schedule deliveries across available vehicles
     */
    public List<DeliveryRoute> scheduleDeliveries(List<DeliveryStop> stops, List<Vehicle> vehicles) {
        log.info("Scheduling {} stops across {} vehicles", stops.size(), vehicles.size());

        // Group stops by time windows and priority
        Map<String, List<DeliveryStop>> timeSlots = groupByTimeSlots(stops);

        List<DeliveryRoute> routes = new ArrayList<>();

        // Create routes for each time slot
        for (Map.Entry<String, List<DeliveryStop>> slot : timeSlots.entrySet()) {
            List<DeliveryStop> slotStops = slot.getValue();

            // Cluster stops by geographic proximity
            List<List<DeliveryStop>> clusters = clusterStops(slotStops, vehicles.size());

            // Assign clusters to vehicles
            for (int i = 0; i < clusters.size() && i < vehicles.size(); i++) {
                Vehicle vehicle = vehicles.get(i);
                List<DeliveryStop> clusterStops = clusters.get(i);

                if (!clusterStops.isEmpty()) {
                    DeliveryRoute route = createRoute(clusterStops, vehicle);
                    routes.add(route);
                }
            }
        }

        log.info("Created {} routes from {} stops", routes.size(), stops.size());

        return routes;
    }

    /**
     * Group stops by time slot (2-hour windows)
     */
    private Map<String, List<DeliveryStop>> groupByTimeSlots(List<DeliveryStop> stops) {
        return stops.stream()
            .collect(Collectors.groupingBy(stop -> {
                if (stop.getWindow() == null) {
                    return "ANYTIME";
                }
                // Round to 2-hour slots
                Instant start = stop.getWindow().getStartTime();
                long hourOfDay = (start.getEpochSecond() / 3600) % 24;
                long slot = hourOfDay / 2;
                return "SLOT_" + slot;
            }));
    }

    /**
     * Cluster stops by geographic proximity using simple k-means
     */
    private List<List<DeliveryStop>> clusterStops(List<DeliveryStop> stops, int k) {
        if (stops.size() <= k) {
            return stops.stream()
                .map(Collections::singletonList)
                .collect(Collectors.toList());
        }

        // Simple geographic clustering by latitude/longitude
        List<List<DeliveryStop>> clusters = new ArrayList<>();

        // Initialize k clusters with random seeds
        List<DeliveryStop> seeds = new ArrayList<>(stops);
        Collections.shuffle(seeds);

        for (int i = 0; i < Math.min(k, stops.size()); i++) {
            List<DeliveryStop> cluster = new ArrayList<>();
            cluster.add(seeds.get(i));
            clusters.add(cluster);
        }

        // Assign remaining stops to nearest cluster
        for (DeliveryStop stop : stops) {
            if (seeds.subList(0, Math.min(k, stops.size())).contains(stop)) {
                continue;
            }

            int nearestCluster = findNearestCluster(stop, clusters);
            clusters.get(nearestCluster).add(stop);
        }

        return clusters;
    }

    /**
     * Find nearest cluster for a stop
     */
    private int findNearestCluster(DeliveryStop stop, List<List<DeliveryStop>> clusters) {
        int nearest = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < clusters.size(); i++) {
            List<DeliveryStop> cluster = clusters.get(i);

            if (cluster.isEmpty()) {
                continue;
            }

            // Calculate average distance to cluster
            double avgDistance = cluster.stream()
                .mapToDouble(s -> stop.getCoordinates().distanceTo(s.getCoordinates()))
                .average()
                .orElse(Double.MAX_VALUE);

            if (avgDistance < minDistance) {
                minDistance = avgDistance;
                nearest = i;
            }
        }

        return nearest;
    }

    /**
     * Create a delivery route from stops
     */
    private DeliveryRoute createRoute(List<DeliveryStop> stops, Vehicle vehicle) {
        DeliveryRoute route = DeliveryRoute.builder()
            .id(UUID.randomUUID().toString())
            .routeNumber(generateRouteNumber())
            .vehicleId(vehicle.getId())
            .driverId(vehicle.getDriverId())
            .stops(new ArrayList<>(stops))
            .plannedStartTime(calculateStartTime(stops))
            .startLocation(vehicle.getHomeBase())
            .currentLocation(vehicle.getCurrentLocation())
            .build();

        // Optimize the route
        return optimizationService.optimizeRoute(route, vehicle);
    }

    /**
     * Calculate optimal start time for route
     */
    private Instant calculateStartTime(List<DeliveryStop> stops) {
        // Find earliest delivery window
        return stops.stream()
            .map(DeliveryStop::getWindow)
            .filter(Objects::nonNull)
            .map(DeliveryWindow::getStartTime)
            .min(Instant::compareTo)
            .orElse(Instant.now());
    }

    /**
     * Generate unique route number
     */
    private String generateRouteNumber() {
        return "R-" + Instant.now().getEpochSecond() + "-" + new Random().nextInt(1000);
    }

    /**
     * Reschedule failed deliveries
     */
    public DeliveryRoute rescheduleFailedDeliveries(List<DeliveryStop> failedStops, Vehicle vehicle) {
        log.info("Rescheduling {} failed deliveries", failedStops.size());

        // Adjust delivery windows for next day
        failedStops.forEach(stop -> {
            if (stop.getWindow() != null) {
                DeliveryWindow newWindow = DeliveryWindow.builder()
                    .startTime(stop.getWindow().getStartTime().plus(24, ChronoUnit.HOURS))
                    .endTime(stop.getWindow().getEndTime().plus(24, ChronoUnit.HOURS))
                    .build();
                stop.setWindow(newWindow);
            }
        });

        return createRoute(failedStops, vehicle);
    }
}
