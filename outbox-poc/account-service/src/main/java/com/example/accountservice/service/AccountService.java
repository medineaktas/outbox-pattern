package com.example.accountservice.service;

import com.example.accountservice.converter.AccountConverter;
import com.example.accountservice.converter.OutboxConverter;
import com.example.accountservice.dto.CreateAccountDto;
import com.example.accountservice.enums.MailStatus;
import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final OutboxService outboxService;

    @Transactional
    public Account createAccount(CreateAccountDto dto) {
        Account newAccount = AccountConverter.fromDto(dto);
        newAccount.setMailStatus(MailStatus.CREATED);
        Account savedAccount = accountRepository.save(newAccount);
        log.info("Account created: {}", savedAccount);
        outboxService.createOutbox(OutboxConverter.convertToOutbox(savedAccount));
        log.info("Outbox created");
        return savedAccount;
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }
}