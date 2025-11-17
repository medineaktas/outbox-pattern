package com.example.accountservice.consumer;

import com.example.accountservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {
    private static final String TOPIC_NAME = "delete-process-byId-from-outbox";
    private static final String GROUP_ID = "delete-process-outbox";

    private final OutboxService outboxService;

    @KafkaListener(
            topics = TOPIC_NAME,
            groupId = GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMessage(
            @Payload String message
    ) {
        try {
            outboxService.deleteById(message);
            log.info("Successfully processed message: {}", message);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
            throw e; // Retry için exception fırlat
        }
    }
}