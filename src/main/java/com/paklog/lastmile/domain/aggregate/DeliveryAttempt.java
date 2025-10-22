package com.paklog.lastmile.domain.aggregate;

import com.paklog.lastmile.domain.valueobject.AttemptResult;
import com.paklog.lastmile.domain.valueobject.GPSCoordinates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAttempt {

    private int attemptNumber;
    private Instant attemptedAt;
    private AttemptResult result;
    private String notes;
    private String driverId;
    private GPSCoordinates location;
    private String photoUrl;
}
