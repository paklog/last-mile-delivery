package com.paklog.lastmile.infrastructure.kafka;

import com.paklog.lastmile.application.port.out.PublishEventPort;
import com.paklog.lastmile.domain.event.DomainEvent;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher implements PublishEventPort {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;

    @Value("${delivery.events.topic}")
    private String topic;

    @Override
    public void publish(DomainEvent event) {
        try {
            CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId(event.getEventId())
                .withType("com.paklog.lastmile." + event.getEventType())
                .withSource(URI.create("https://paklog.com/lastmile"))
                .withTime(event.getOccurredAt().atOffset(java.time.ZoneOffset.UTC))
                .withData("application/json", event.toString().getBytes())
                .build();

            kafkaTemplate.send(topic, event.getEventId(), cloudEvent);

            log.info("Published event: {} to topic: {}", event.getEventType(), topic);

        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getEventType(), e);
        }
    }
}
