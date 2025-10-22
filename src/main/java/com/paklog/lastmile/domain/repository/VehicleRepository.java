package com.paklog.lastmile.domain.repository;

import com.paklog.lastmile.domain.aggregate.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository {

    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(String id);

    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    List<Vehicle> findAvailableVehicles();

    List<Vehicle> findByDriverId(String driverId);

    List<Vehicle> findAll();

    void deleteById(String id);
}
