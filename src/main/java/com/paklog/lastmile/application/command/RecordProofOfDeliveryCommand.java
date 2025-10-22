package com.paklog.lastmile.application.command;

import com.paklog.lastmile.domain.valueobject.GPSCoordinates;
import com.paklog.lastmile.domain.valueobject.Signature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordProofOfDeliveryCommand {

    @NotBlank
    private String routeId;

    @NotBlank
    private String stopId;

    @NotEmpty
    private List<String> packageIds;

    @NotNull
    private Signature signature;

    private List<String> photoUrls;

    @NotNull
    private GPSCoordinates deliveryLocation;

    @NotBlank
    private String recipientName;

    private String recipientRelation;
    private String notes;
}
