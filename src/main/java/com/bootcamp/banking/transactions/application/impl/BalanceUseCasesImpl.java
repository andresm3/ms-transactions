package com.bootcamp.banking.transactions.application.impl;

import com.bootcamp.banking.transactions.domain.models.Balance;
import com.bootcamp.banking.transactions.infraestructure.repository.BalanceRepository;
import com.bootcamp.banking.transactions.application.BalanceUseCases;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class BalanceUseCasesImpl implements BalanceUseCases {

  private final BalanceRepository balanceRepository;
  @Override
  public Flux<Balance> getBalanceByClient(String clientId) {
    return null;
  }

  @Override
  public Flux<Balance> getBalanceByClientAndProductType(String clientId, String productType) {
    return null;
  }
}
