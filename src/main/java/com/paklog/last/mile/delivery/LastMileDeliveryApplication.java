package com.paklog.last.mile.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Last Mile Delivery
 *
 * Route optimization and delivery tracking coordination
 *
 * @author Paklog Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableKafka
@EnableMongoAuditing
public class LastMileDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LastMileDeliveryApplication.class, args);
    }
}