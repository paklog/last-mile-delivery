package com.paklog.last.mile.delivery.domain.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * DeliveryRoute Aggregate Root
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deliveryroutes")
public class DeliveryRoute {

    @Id
    private String id;

    private Instant createdAt;
    private Instant updatedAt;

    // Domain logic methods here
}