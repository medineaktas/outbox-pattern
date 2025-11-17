package com.example.mailservice.service;

import com.example.mailservice.publisher.KafkaPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
    private final KafkaPublisher kafkaPublisher;

    public void deleteProcessByIdFromOutbox(String id) {
        log.info("MailService delete proccess: {}", id);
        kafkaPublisher.publish("delete-process-byId-from-outbox", id);
    }
    public void sendMail(String username, String id) throws Exception {
        if(username.equals("test")) {
            throw new Exception("Simulated exception case");
        }
        kafkaPublisher.publish("send-mail", username);
        log.info("Hello,{} Nice to meet you!", username);
    }
}