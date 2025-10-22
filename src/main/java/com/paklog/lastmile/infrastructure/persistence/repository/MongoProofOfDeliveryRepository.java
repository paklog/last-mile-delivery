package com.paklog.lastmile.infrastructure.persistence.repository;

import com.paklog.lastmile.domain.aggregate.ProofOfDelivery;
import com.paklog.lastmile.domain.repository.ProofOfDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoProofOfDeliveryRepository implements ProofOfDeliveryRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public ProofOfDelivery save(ProofOfDelivery proof) {
        return mongoTemplate.save(proof);
    }

    @Override
    public Optional<ProofOfDelivery> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, ProofOfDelivery.class));
    }

    @Override
    public List<ProofOfDelivery> findByRouteId(String routeId) {
        Query query = new Query(Criteria.where("deliveryRouteId").is(routeId));
        return mongoTemplate.find(query, ProofOfDelivery.class);
    }

    @Override
    public List<ProofOfDelivery> findByStopId(String stopId) {
        Query query = new Query(Criteria.where("stopId").is(stopId));
        return mongoTemplate.find(query, ProofOfDelivery.class);
    }

    @Override
    public List<ProofOfDelivery> findByDriverId(String driverId) {
        Query query = new Query(Criteria.where("driverId").is(driverId));
        return mongoTemplate.find(query, ProofOfDelivery.class);
    }

    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, ProofOfDelivery.class);
    }
}
