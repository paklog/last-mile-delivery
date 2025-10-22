package com.paklog.lastmile.application.command;

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
public class UpdateTrafficCommand {

    @NotBlank
    private String routeId;

    @NotNull
    private Double delayMinutes;
}
