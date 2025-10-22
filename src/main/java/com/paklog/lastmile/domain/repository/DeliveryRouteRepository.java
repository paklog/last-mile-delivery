package com.paklog.lastmile.domain.repository;

import com.paklog.lastmile.domain.aggregate.DeliveryRoute;
import com.paklog.lastmile.domain.valueobject.RouteStatus;

import java.util.List;
import java.util.Optional;

public interface DeliveryRouteRepository {

    DeliveryRoute save(DeliveryRoute route);

    Optional<DeliveryRoute> findById(String id);

    List<DeliveryRoute> findByStatus(RouteStatus status);

    List<DeliveryRoute> findByDriverId(String driverId);

    List<DeliveryRoute> findByVehicleId(String vehicleId);

    List<DeliveryRoute> findActiveRoutes();

    void deleteById(String id);
}
