package com.paklog.lastmile.domain.aggregate;

import com.paklog.lastmile.domain.valueobject.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStop {

    private String stopId;
    private int sequence;
    private StopType type;

    private String customerId;
    private String customerName;
    private String customerPhone;
    private String address;
    private GPSCoordinates coordinates;

    private DeliveryWindow window;
    private int estimatedDurationMinutes;

    private List<String> packageIds;
    private int totalWeightKg;
    private String specialInstructions;

    private Instant estimatedArrival;
    private Instant actualArrival;
    private Instant actualDeparture;

    private boolean completed;
    private AttemptResult attemptResult;
    private String notes;

    @Builder.Default
    private List<DeliveryAttempt> attempts = new ArrayList<>();

    public void recordAttempt(DeliveryAttempt attempt) {
        this.attempts.add(attempt);

        if (attempt.getResult() == AttemptResult.SUCCESSFUL) {
            this.completed = true;
            this.attemptResult = AttemptResult.SUCCESSFUL;
        }
    }

    public int getAttemptCount() {
        return attempts.size();
    }

    public boolean hasFailedMaxAttempts() {
        return attempts.size() >= 3 && !completed;
    }

    public boolean isWithinWindow(Instant time) {
        return window != null && window.contains(time);
    }

    public void updateETA(Instant eta) {
        this.estimatedArrival = eta;
    }
}
