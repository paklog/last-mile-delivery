package com.paklog.lastmile.domain.repository;

import com.paklog.lastmile.domain.aggregate.ProofOfDelivery;

import java.util.List;
import java.util.Optional;

public interface ProofOfDeliveryRepository {

    ProofOfDelivery save(ProofOfDelivery proof);

    Optional<ProofOfDelivery> findById(String id);

    List<ProofOfDelivery> findByRouteId(String routeId);

    List<ProofOfDelivery> findByStopId(String stopId);

    List<ProofOfDelivery> findByDriverId(String driverId);

    void deleteById(String id);
}
