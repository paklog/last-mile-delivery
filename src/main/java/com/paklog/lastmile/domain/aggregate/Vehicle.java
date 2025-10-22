package com.paklog.lastmile.domain.aggregate;

import com.paklog.lastmile.domain.valueobject.GPSCoordinates;
import com.paklog.lastmile.domain.valueobject.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "vehicles")
public class Vehicle {

    @Id
    private String id;

    private String vehicleNumber;
    private String licensePlate;
    private VehicleType type;

    private String driverId;
    private String driverName;
    private String driverPhone;

    private GPSCoordinates currentLocation;
    private GPSCoordinates homeBase;

    private int currentWeightKg;
    private boolean available;
    private boolean active;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public boolean canAccommodate(int weightKg, int stops) {
        return available &&
               currentWeightKg + weightKg <= type.getMaxWeightKg() &&
               stops <= type.getMaxStops();
    }

    public void assignRoute() {
        this.available = false;
    }

    public void completeRoute() {
        this.available = true;
        this.currentWeightKg = 0;
    }

    public void updateLocation(GPSCoordinates location) {
        this.currentLocation = location;
        this.updatedAt = Instant.now();
    }
}
