package com.bootcamp.banking.transactions.infraestructure.rest;

import com.bootcamp.banking.transactions.application.BalanceUseCases;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/balances")
public class BalanceResource {

  private final BalanceUseCases balanceUseCases;
}
