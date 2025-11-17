package com.example.accountservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Data
@Builder
@Entity
@Table(name = "outboxs")
@AllArgsConstructor
@NoArgsConstructor
public class Outbox {
    @Id
    @UuidGenerator
    private String id;

    private String type;

    @Column(length = 4000)
    private String payload;
}