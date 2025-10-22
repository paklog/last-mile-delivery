package com.paklog.lastmile.infrastructure.persistence.repository;

import com.paklog.lastmile.domain.aggregate.Vehicle;
import com.paklog.lastmile.domain.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoVehicleRepository implements VehicleRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Vehicle save(Vehicle vehicle) {
        return mongoTemplate.save(vehicle);
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, Vehicle.class));
    }

    @Override
    public Optional<Vehicle> findByVehicleNumber(String vehicleNumber) {
        Query query = new Query(Criteria.where("vehicleNumber").is(vehicleNumber));
        return Optional.ofNullable(mongoTemplate.findOne(query, Vehicle.class));
    }

    @Override
    public List<Vehicle> findAvailableVehicles() {
        Query query = new Query(Criteria.where("available").is(true).and("active").is(true));
        return mongoTemplate.find(query, Vehicle.class);
    }

    @Override
    public List<Vehicle> findByDriverId(String driverId) {
        Query query = new Query(Criteria.where("driverId").is(driverId));
        return mongoTemplate.find(query, Vehicle.class);
    }

    @Override
    public List<Vehicle> findAll() {
        return mongoTemplate.findAll(Vehicle.class);
    }

    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, Vehicle.class);
    }
}
