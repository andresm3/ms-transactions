package com.bootcamp.banking.transactions.application;

import com.bootcamp.banking.transactions.domain.models.Balance;
import reactor.core.publisher.Flux;

public interface BalanceUseCases {

  Flux<Balance> getBalanceByClient(String clientId);
  Flux<Balance> getBalanceByClientAndProductType(String clientId, String productType);
}
