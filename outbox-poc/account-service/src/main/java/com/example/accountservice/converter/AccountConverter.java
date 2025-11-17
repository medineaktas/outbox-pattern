package com.example.accountservice.converter;

import com.example.accountservice.dto.CreateAccountDto;
import com.example.accountservice.model.Account;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AccountConverter {
    public static Account fromDto(CreateAccountDto dto) {
        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setMail(dto.getMail());
        account.setPassword(dto.getPassword());
        return account;
    }
}