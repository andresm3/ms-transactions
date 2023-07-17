package com.bootcamp.banking.transactions.transactions.application.impl;

import com.bootcamp.banking.transactions.transactions.application.TransactionUseCases;
import com.bootcamp.banking.transactions.transactions.domain.models.Transaction;
import com.bootcamp.banking.transactions.transactions.infraestructure.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class TransactionUseCasesImpl implements TransactionUseCases {

  private final TransactionRepository transactionRepository;
//  @Override
//  public Flux<Transaction> getTransactionsByClient(String clientId) {
//    return null;
//  }
//
//  @Override
//  public Flux<Transaction> getTransactionsByClientAndProductType(String clientId,
//      String productType) {
//    return null;
//  }
}
