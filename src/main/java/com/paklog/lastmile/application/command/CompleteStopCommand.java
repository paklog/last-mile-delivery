package com.paklog.lastmile.application.command;

import com.paklog.lastmile.domain.aggregate.DeliveryAttempt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteStopCommand {

    @NotBlank
    private String routeId;

    @NotBlank
    private String stopId;

    @NotNull
    private Boolean success;

    private DeliveryAttempt attempt;
}
