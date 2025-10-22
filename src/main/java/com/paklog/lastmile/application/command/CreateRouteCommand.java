package com.paklog.lastmile.application.command;

import com.paklog.lastmile.domain.aggregate.DeliveryStop;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRouteCommand {

    @NotBlank
    private String vehicleId;

    @NotEmpty
    private List<DeliveryStop> stops;

    private Instant plannedStartTime;
}
