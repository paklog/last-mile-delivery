package com.paklog.lastmile.domain.aggregate;

import com.paklog.lastmile.domain.valueobject.GPSCoordinates;
import com.paklog.lastmile.domain.valueobject.Signature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "proofs_of_delivery")
public class ProofOfDelivery {

    @Id
    private String id;

    private String deliveryRouteId;
    private String stopId;
    private List<String> packageIds;

    private Signature signature;

    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();

    private GPSCoordinates deliveryLocation;
    private Instant deliveredAt;

    private String driverId;
    private String driverName;

    private String recipientName;
    private String recipientRelation;

    private String notes;

    @CreatedDate
    private Instant createdAt;

    public boolean isValid() {
        return signature != null &&
               !packageIds.isEmpty() &&
               deliveredAt != null &&
               recipientName != null &&
               deliveryLocation != null &&
               deliveryLocation.isValid();
    }

    public void addPhoto(String photoUrl) {
        if (this.photoUrls == null) {
            this.photoUrls = new ArrayList<>();
        }
        this.photoUrls.add(photoUrl);
    }
}
