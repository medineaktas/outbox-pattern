package com.example.mailservice.consumer;

import com.example.mailservice.model.KafkaPayload;
import com.example.mailservice.service.MailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private static final String TOPIC_NAME = "account-created";
    private static final String GROUP_ID = "GroupId";

    private final MailService mailService;
    private final ObjectMapper MAPPER = new ObjectMapper();

    @KafkaListener(topics = TOPIC_NAME, groupId = GROUP_ID, containerFactory = "kafkaListenerContainerFactory")
    public void listener(String message) throws Exception {
        // Doğrudan mesajı alıyoruz, tırnaklar yok
        log.info("Raw message: {}", message);

        // JSON parse
        JsonNode payload = MAPPER.readTree(message);
        log.info("JSON NODE: {}", payload);

        KafkaPayload kafkaPayload = MAPPER.readValue(payload.get("payload").asText(), KafkaPayload.class);
        log.info("KafkaPayload: {}", kafkaPayload);
        log.info("KafkaPayload getId: {}", kafkaPayload.getId());

        // İşlemler

        mailService.sendMail(kafkaPayload.getUsername(), kafkaPayload.getId());
        mailService.deleteProcessByIdFromOutbox(kafkaPayload.getId());


    }
}
