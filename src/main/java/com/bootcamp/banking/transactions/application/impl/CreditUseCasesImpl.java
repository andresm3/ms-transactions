package com.bootcamp.banking.transactions.application.impl;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.bootcamp.banking.transactions.application.CreditUseCases;
import com.bootcamp.banking.transactions.application.exceptions.customs.CustomNotFoundException;
import com.bootcamp.banking.transactions.domain.dto.response.CreditResponse;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class CreditUseCasesImpl implements CreditUseCases {

  private final String urlCredit = "http://localhost:8083/credits";

  @Autowired
//  @Qualifier("wcLoadBalanced")
  private WebClient.Builder webClient;

  @Override
  public Mono<CreditResponse> findCredit(String number) {
    return webClient
        .build()
        .get()
        .uri(urlCredit + "/number/{number}", number)
        .retrieve()
        .onStatus(NOT_FOUND::equals, response -> Mono
            .error(new CustomNotFoundException("Credit " + number + " not found")))
        .bodyToMono(CreditResponse.class);
  }

  @Override
  public void updateCredit(String id, BigDecimal amount) {
    System.out.println(">>updateCredit>/balance/{id}/amount/{amount}>>>>>>>>> " + amount);
    webClient
        .build()
        .put()
        .uri(urlCredit + "/balance/{id}/amount/{amount}", id, amount)
        .retrieve()
        .bodyToMono(Void.class)
        .subscribe();
  }
}
