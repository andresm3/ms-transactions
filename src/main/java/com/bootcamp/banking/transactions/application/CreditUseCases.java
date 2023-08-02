package com.bootcamp.banking.transactions.application;

import com.bootcamp.banking.transactions.domain.dto.response.CreditResponse;
import java.math.BigDecimal;
import reactor.core.publisher.Mono;

public interface CreditUseCases {

  Mono<CreditResponse> findCredit(String number);

  void updateCredit(String id, BigDecimal amount);
}
