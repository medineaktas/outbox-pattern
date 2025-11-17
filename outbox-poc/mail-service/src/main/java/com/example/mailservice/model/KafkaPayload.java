package com.example.mailservice.model;
import lombok.Data;

@Data
public class KafkaPayload {
    private String id;
    private String username;
    private String mail;
    private String password;
    private String mailStatus;
    private long createdDate;
}