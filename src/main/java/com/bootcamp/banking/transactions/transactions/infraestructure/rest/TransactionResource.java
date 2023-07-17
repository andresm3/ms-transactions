package com.bootcamp.banking.transactions.transactions.infraestructure.rest;

import com.bootcamp.banking.transactions.transactions.application.TransactionUseCases;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionResource {

  private final TransactionUseCases transactionUseCases;
}
