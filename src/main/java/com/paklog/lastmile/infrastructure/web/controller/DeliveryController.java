package com.paklog.lastmile.infrastructure.web.controller;

import com.paklog.lastmile.application.command.*;
import com.paklog.lastmile.application.port.in.DeliveryCoordinationUseCase;
import com.paklog.lastmile.domain.aggregate.DeliveryRoute;
import com.paklog.lastmile.domain.aggregate.ProofOfDelivery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Tag(name = "Last-Mile Delivery", description = "Last-mile delivery coordination")
public class DeliveryController {

    private final DeliveryCoordinationUseCase deliveryUseCase;

    @PostMapping("/routes")
    @Operation(summary = "Create delivery route")
    public ResponseEntity<String> createRoute(@Valid @RequestBody CreateRouteCommand command) {
        log.info("REST: Creating delivery route");
        String routeId = deliveryUseCase.createRoute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(routeId);
    }

    @PostMapping("/routes/{id}/start")
    @Operation(summary = "Start delivery route")
    public ResponseEntity<Void> startRoute(@PathVariable String id) {
        log.info("REST: Starting route: {}", id);
        StartRouteCommand command = StartRouteCommand.builder()
            .routeId(id)
            .build();
        deliveryUseCase.startRoute(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/routes/{routeId}/stops/{stopId}/complete")
    @Operation(summary = "Complete delivery stop")
    public ResponseEntity<Void> completeStop(
        @PathVariable String routeId,
        @PathVariable String stopId,
        @Valid @RequestBody CompleteStopCommand command) {
        log.info("REST: Completing stop {} on route {}", stopId, routeId);
        command.setRouteId(routeId);
        command.setStopId(stopId);
        deliveryUseCase.completeStop(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/proof-of-delivery")
    @Operation(summary = "Record proof of delivery")
    public ResponseEntity<String> recordProofOfDelivery(@Valid @RequestBody RecordProofOfDeliveryCommand command) {
        log.info("REST: Recording proof of delivery");
        String proofId = deliveryUseCase.recordProofOfDelivery(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(proofId);
    }

    @PostMapping("/routes/{id}/optimize")
    @Operation(summary = "Optimize route")
    public ResponseEntity<Void> optimizeRoute(@PathVariable String id) {
        log.info("REST: Optimizing route: {}", id);
        OptimizeRouteCommand command = OptimizeRouteCommand.builder()
            .routeId(id)
            .build();
        deliveryUseCase.optimizeRoute(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/routes/{id}/traffic")
    @Operation(summary = "Update traffic conditions")
    public ResponseEntity<Void> updateTraffic(
        @PathVariable String id,
        @RequestParam Double delayMinutes) {
        log.info("REST: Updating traffic for route: {}", id);
        UpdateTrafficCommand command = UpdateTrafficCommand.builder()
            .routeId(id)
            .delayMinutes(delayMinutes)
            .build();
        deliveryUseCase.updateTrafficConditions(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/routes/{id}")
    @Operation(summary = "Get route by ID")
    public ResponseEntity<DeliveryRoute> getRoute(@PathVariable String id) {
        DeliveryRoute route = deliveryUseCase.getRoute(id);
        return ResponseEntity.ok(route);
    }

    @GetMapping("/routes/active")
    @Operation(summary = "Get all active routes")
    public ResponseEntity<List<DeliveryRoute>> getActiveRoutes() {
        List<DeliveryRoute> routes = deliveryUseCase.getActiveRoutes();
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/routes/driver/{driverId}")
    @Operation(summary = "Get routes by driver")
    public ResponseEntity<List<DeliveryRoute>> getRoutesByDriver(@PathVariable String driverId) {
        List<DeliveryRoute> routes = deliveryUseCase.getRoutesByDriver(driverId);
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/proof-of-delivery/{id}")
    @Operation(summary = "Get proof of delivery")
    public ResponseEntity<ProofOfDelivery> getProofOfDelivery(@PathVariable String id) {
        ProofOfDelivery proof = deliveryUseCase.getProofOfDelivery(id);
        return ResponseEntity.ok(proof);
    }
}
