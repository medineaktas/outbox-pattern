package com.example.accountservice.model;
import com.example.accountservice.enums.MailStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;

@Data
@Entity
@Table(name = "accounts")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @UuidGenerator
    private String id;

    private String username;

    private String mail;

    private String password;

    private MailStatus mailStatus;

    @Column(name = "created_date")
    private Date createdDate;

    @PrePersist
    private void prePersist() {
        createdDate = new Date();
    }
}