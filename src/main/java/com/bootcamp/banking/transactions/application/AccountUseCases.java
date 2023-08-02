package com.bootcamp.banking.transactions.application;

import com.bootcamp.banking.transactions.domain.dto.response.AccountResponse;
import java.math.BigDecimal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountUseCases {

  Flux<AccountResponse> listByDebitCard(String debitCard);

  Mono<AccountResponse> findAccount(String number);

  Mono<AccountResponse> findAccountById(String number);

  Mono<BigDecimal> getTotalBalanceByDebitCard(String debitCard);

  void updateAccount(String id, BigDecimal amount);
}
