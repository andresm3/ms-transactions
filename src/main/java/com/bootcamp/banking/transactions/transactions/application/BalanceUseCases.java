package com.bootcamp.banking.transactions.transactions.application;

import com.bootcamp.banking.transactions.transactions.domain.models.Balance;
import reactor.core.publisher.Flux;

public interface BalanceUseCases {

  Flux<Balance> getBalanceByClient(String clientId);
  Flux<Balance> getBalanceByClientAndProductType(String clientId, String productType);
}
