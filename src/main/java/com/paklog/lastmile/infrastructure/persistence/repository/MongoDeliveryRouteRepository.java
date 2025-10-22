package com.paklog.lastmile.infrastructure.persistence.repository;

import com.paklog.lastmile.domain.aggregate.DeliveryRoute;
import com.paklog.lastmile.domain.repository.DeliveryRouteRepository;
import com.paklog.lastmile.domain.valueobject.RouteStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoDeliveryRouteRepository implements DeliveryRouteRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public DeliveryRoute save(DeliveryRoute route) {
        return mongoTemplate.save(route);
    }

    @Override
    public Optional<DeliveryRoute> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, DeliveryRoute.class));
    }

    @Override
    public List<DeliveryRoute> findByStatus(RouteStatus status) {
        Query query = new Query(Criteria.where("status").is(status));
        return mongoTemplate.find(query, DeliveryRoute.class);
    }

    @Override
    public List<DeliveryRoute> findByDriverId(String driverId) {
        Query query = new Query(Criteria.where("driverId").is(driverId));
        return mongoTemplate.find(query, DeliveryRoute.class);
    }

    @Override
    public List<DeliveryRoute> findByVehicleId(String vehicleId) {
        Query query = new Query(Criteria.where("vehicleId").is(vehicleId));
        return mongoTemplate.find(query, DeliveryRoute.class);
    }

    @Override
    public List<DeliveryRoute> findActiveRoutes() {
        Query query = new Query(Criteria.where("status").in(RouteStatus.PLANNED, RouteStatus.IN_PROGRESS));
        return mongoTemplate.find(query, DeliveryRoute.class);
    }

    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, DeliveryRoute.class);
    }
}
