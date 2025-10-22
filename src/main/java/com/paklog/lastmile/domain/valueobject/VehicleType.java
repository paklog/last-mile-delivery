package com.paklog.lastmile.domain.valueobject;

import lombok.Getter;

@Getter
public enum VehicleType {
    VAN(1500, 20),
    TRUCK(5000, 40),
    BIKE(50, 5),
    CARGO_VAN(2500, 30);

    private final int maxWeightKg;
    private final int maxStops;

    VehicleType(int maxWeightKg, int maxStops) {
        this.maxWeightKg = maxWeightKg;
        this.maxStops = maxStops;
    }
}
