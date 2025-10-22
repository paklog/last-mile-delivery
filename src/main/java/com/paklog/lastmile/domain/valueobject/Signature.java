package com.paklog.lastmile.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Signature {
    private String signatureImageUrl;
    private String signerName;
    private Instant signedAt;
    private String deviceId;
    private GPSCoordinates location;
}
