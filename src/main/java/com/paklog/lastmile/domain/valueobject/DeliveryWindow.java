package com.paklog.lastmile.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryWindow {
    private Instant startTime;
    private Instant endTime;

    public boolean contains(Instant time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    public boolean overlaps(DeliveryWindow other) {
        return !this.endTime.isBefore(other.startTime) &&
               !other.endTime.isBefore(this.startTime);
    }

    public long durationMinutes() {
        return ChronoUnit.MINUTES.between(startTime, endTime);
    }

    public boolean isValid() {
        return startTime != null && endTime != null &&
               startTime.isBefore(endTime) &&
               durationMinutes() >= 30 && durationMinutes() <= 240; // 30 min to 4 hours
    }
}
