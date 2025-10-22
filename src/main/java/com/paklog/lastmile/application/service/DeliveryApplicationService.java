package com.paklog.lastmile.application.service;

import com.paklog.lastmile.application.command.*;
import com.paklog.lastmile.application.port.in.DeliveryCoordinationUseCase;
import com.paklog.lastmile.application.port.out.PublishEventPort;
import com.paklog.lastmile.domain.aggregate.DeliveryRoute;
import com.paklog.lastmile.domain.aggregate.DeliveryStop;
import com.paklog.lastmile.domain.aggregate.ProofOfDelivery;
import com.paklog.lastmile.domain.aggregate.Vehicle;
import com.paklog.lastmile.domain.repository.DeliveryRouteRepository;
import com.paklog.lastmile.domain.repository.ProofOfDeliveryRepository;
import com.paklog.lastmile.domain.repository.VehicleRepository;
import com.paklog.lastmile.domain.service.RouteOptimizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryApplicationService implements DeliveryCoordinationUseCase {

    private final DeliveryRouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;
    private final ProofOfDeliveryRepository proofRepository;
    private final RouteOptimizationService optimizationService;
    private final PublishEventPort publishEventPort;

    @Override
    @Transactional
    public String createRoute(CreateRouteCommand command) {
        log.info("Creating delivery route with {} stops", command.getStops().size());

        Vehicle vehicle = vehicleRepository.findById(command.getVehicleId())
            .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + command.getVehicleId()));

        // Validate vehicle can accommodate route
        int totalWeight = command.getStops().stream()
            .mapToInt(DeliveryStop::getTotalWeightKg)
            .sum();

        if (!vehicle.canAccommodate(totalWeight, command.getStops().size())) {
            throw new IllegalStateException("Vehicle cannot accommodate this route");
        }

        // Create route
        DeliveryRoute route = DeliveryRoute.builder()
            .id(UUID.randomUUID().toString())
            .routeNumber(generateRouteNumber())
            .vehicleId(vehicle.getId())
            .driverId(vehicle.getDriverId())
            .stops(new ArrayList<>(command.getStops()))
            .plannedStartTime(command.getPlannedStartTime() != null ?
                command.getPlannedStartTime() : Instant.now())
            .startLocation(vehicle.getHomeBase())
            .currentLocation(vehicle.getCurrentLocation())
            .build();

        // Optimize route
        route = optimizationService.optimizeRoute(route, vehicle);

        // Plan the route
        route.plan();

        // Save route
        route = routeRepository.save(route);

        // Publish events
        route.getDomainEvents().forEach(publishEventPort::publish);
        route.clearDomainEvents();

        // Mark vehicle as assigned
        vehicle.assignRoute();
        vehicleRepository.save(vehicle);

        log.info("Route created: {}", route.getId());

        return route.getId();
    }

    @Override
    @Transactional
    public void startRoute(StartRouteCommand command) {
        log.info("Starting route: {}", command.getRouteId());

        DeliveryRoute route = routeRepository.findById(command.getRouteId())
            .orElseThrow(() -> new IllegalArgumentException("Route not found"));

        route.start();

        routeRepository.save(route);

        route.getDomainEvents().forEach(publishEventPort::publish);
        route.clearDomainEvents();
    }

    @Override
    @Transactional
    public void completeStop(CompleteStopCommand command) {
        log.info("Completing stop {} on route {}", command.getStopId(), command.getRouteId());

        DeliveryRoute route = routeRepository.findById(command.getRouteId())
            .orElseThrow(() -> new IllegalArgumentException("Route not found"));

        // Find and update stop
        DeliveryStop stop = route.getStops().stream()
            .filter(s -> s.getStopId().equals(command.getStopId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Stop not found"));

        // Record attempt
        if (command.getAttempt() != null) {
            stop.recordAttempt(command.getAttempt());
        }

        // Complete stop in route
        route.completeStop(command.getStopId(), command.getSuccess());

        routeRepository.save(route);

        route.getDomainEvents().forEach(publishEventPort::publish);
        route.clearDomainEvents();
    }

    @Override
    @Transactional
    public String recordProofOfDelivery(RecordProofOfDeliveryCommand command) {
        log.info("Recording proof of delivery for stop {}", command.getStopId());

        ProofOfDelivery proof = ProofOfDelivery.builder()
            .id(UUID.randomUUID().toString())
            .deliveryRouteId(command.getRouteId())
            .stopId(command.getStopId())
            .packageIds(command.getPackageIds())
            .signature(command.getSignature())
            .photoUrls(command.getPhotoUrls())
            .deliveryLocation(command.getDeliveryLocation())
            .deliveredAt(Instant.now())
            .recipientName(command.getRecipientName())
            .recipientRelation(command.getRecipientRelation())
            .notes(command.getNotes())
            .build();

        if (!proof.isValid()) {
            throw new IllegalArgumentException("Invalid proof of delivery");
        }

        proof = proofRepository.save(proof);

        log.info("Proof of delivery recorded: {}", proof.getId());

        return proof.getId();
    }

    @Override
    @Transactional
    public void optimizeRoute(OptimizeRouteCommand command) {
        log.info("Optimizing route: {}", command.getRouteId());

        DeliveryRoute route = routeRepository.findById(command.getRouteId())
            .orElseThrow(() -> new IllegalArgumentException("Route not found"));

        Vehicle vehicle = vehicleRepository.findById(route.getVehicleId())
            .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        route = optimizationService.optimizeRoute(route, vehicle);

        routeRepository.save(route);

        route.getDomainEvents().forEach(publishEventPort::publish);
        route.clearDomainEvents();
    }

    @Override
    @Transactional
    public void updateTrafficConditions(UpdateTrafficCommand command) {
        log.info("Updating traffic for route: {}", command.getRouteId());

        DeliveryRoute route = routeRepository.findById(command.getRouteId())
            .orElseThrow(() -> new IllegalArgumentException("Route not found"));

        route.updateTrafficDelay(command.getDelayMinutes());

        routeRepository.save(route);

        route.getDomainEvents().forEach(publishEventPort::publish);
        route.clearDomainEvents();
    }

    @Override
    public DeliveryRoute getRoute(String routeId) {
        return routeRepository.findById(routeId)
            .orElseThrow(() -> new IllegalArgumentException("Route not found"));
    }

    @Override
    public List<DeliveryRoute> getActiveRoutes() {
        return routeRepository.findActiveRoutes();
    }

    @Override
    public List<DeliveryRoute> getRoutesByDriver(String driverId) {
        return routeRepository.findByDriverId(driverId);
    }

    @Override
    public ProofOfDelivery getProofOfDelivery(String proofId) {
        return proofRepository.findById(proofId)
            .orElseThrow(() -> new IllegalArgumentException("Proof of delivery not found"));
    }

    private String generateRouteNumber() {
        return "ROUTE-" + Instant.now().getEpochSecond();
    }
}
