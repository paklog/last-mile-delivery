package com.paklog.lastmile.application.port.in;

import com.paklog.lastmile.application.command.*;
import com.paklog.lastmile.domain.aggregate.DeliveryRoute;
import com.paklog.lastmile.domain.aggregate.ProofOfDelivery;

import java.util.List;

public interface DeliveryCoordinationUseCase {

    String createRoute(CreateRouteCommand command);

    void startRoute(StartRouteCommand command);

    void completeStop(CompleteStopCommand command);

    String recordProofOfDelivery(RecordProofOfDeliveryCommand command);

    void optimizeRoute(OptimizeRouteCommand command);

    void updateTrafficConditions(UpdateTrafficCommand command);

    DeliveryRoute getRoute(String routeId);

    List<DeliveryRoute> getActiveRoutes();

    List<DeliveryRoute> getRoutesByDriver(String driverId);

    ProofOfDelivery getProofOfDelivery(String proofId);
}
