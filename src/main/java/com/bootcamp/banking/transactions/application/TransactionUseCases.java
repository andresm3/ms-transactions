package com.bootcamp.banking.transactions.application;

import com.bootcamp.banking.transactions.domain.dto.request.FilterRequest;
import com.bootcamp.banking.transactions.domain.dto.request.TransactionRequest;
import com.bootcamp.banking.transactions.domain.models.Transaction;
import java.math.BigDecimal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionUseCases {

//  Flux<Transaction> getTransactionsByClient(String clientId);
//  Flux<Transaction> getTransactionsByClientAndProductType(String clientId, String productType);
  Flux<Transaction> getTransactionsByAccountNumber(String accountNumber);

  Flux<Transaction> getTransactionsByCreditNumber(String creditNumber);

  Flux<Transaction> listAccountTransactionsWithCommission(String accountNumber,
      FilterRequest request);

  Mono<String> depositAccount(String accountNumber, TransactionRequest request);

  Mono<String> withdrawalAccount(String accountNumber, TransactionRequest request);

  Mono<String> withdrawalFromDebitCard(String debitCard, TransactionRequest request);

  Mono<String> transferBetweenAccounts(String exitNumber, String entryNumber,
      TransactionRequest request);

  Mono<String> transferAmongAccounts(String outPhoneNumber, String inPhoneNumber,
      BigDecimal amount, String description);

  Mono<String> payCredit(String creditNumber, BigDecimal amount);

  Mono<String> spendCredit(String creditNumber, BigDecimal amount);
}
