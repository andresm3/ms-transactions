package com.bootcamp.banking.transactions.application.impl;

import static com.bootcamp.banking.transactions.application.utils.Constants.AccountType.FIXED_TERM;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.bootcamp.banking.transactions.application.AccountUseCases;
import com.bootcamp.banking.transactions.application.exceptions.customs.CustomInformationException;
import com.bootcamp.banking.transactions.application.exceptions.customs.CustomNotFoundException;
import com.bootcamp.banking.transactions.domain.dto.response.AccountResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountUseCasesImpl implements AccountUseCases {

  private static final String NOT_FOUND_MESSAGE = " not found";
  private final String urlAccount = "http://localhost:8082/accounts";
  @Autowired
//  @Qualifier("wcLoadBalanced")
  private WebClient.Builder webClient;

  @Override
  public Flux<AccountResponse> listByDebitCard(String debitCard) {
    return webClient
        .build()
        .get()
        .uri(urlAccount + "/debitCard/{debitCard}", debitCard)
        .retrieve()
        .onStatus(status -> status == NOT_FOUND, response -> Mono
            .error(new CustomNotFoundException("Debit card " + debitCard + NOT_FOUND_MESSAGE)))
        .bodyToFlux(AccountResponse.class);
  }

  @Override
  public Mono<AccountResponse> findAccount(String number) {
    System.out.println("--findAccount------- " + number);
        return webClient
        .build()
        .get()
        .uri(urlAccount + "/number/{number}", number)
        .retrieve()
        .onStatus(NOT_FOUND::equals, response ->
            Mono.error(new CustomNotFoundException("Account " + number + NOT_FOUND_MESSAGE)))
        .bodyToMono(AccountResponse.class)
        .onErrorStop()
        .flatMap(account -> {
          if (account.getTypeAccount().getOption() == FIXED_TERM) {
            int currentDay = LocalDate.now().getDayOfMonth();
            if (currentDay != account.getTypeAccount().getDay()) {
              return Mono.error(new CustomInformationException("Only the day "
                  + account.getTypeAccount().getDay()
                  + " of each month you can make a transaction for your account"));
            }
          }

          return Mono.just(account);
        });
  }

  @Override
  public Mono<AccountResponse> findAccountById(String id) {
    System.out.println("--findAccountById----- " + id);
    return webClient
        .build()
        .get()
        .uri(urlAccount + "/id/{id}", id)
        .retrieve()
        .bodyToMono(AccountResponse.class)
        .flatMap(account -> {
          System.out.println(">>>acc received>>>>>> " + account);

          return Mono.just(account);
        });
  }
  @Override
  public Mono<BigDecimal> getTotalBalanceByDebitCard(String debitCard) {
    return webClient
        .build()
        .get()
        .uri(urlAccount + "/totalBalance/{debitCard}", debitCard)
        .retrieve()
        .onStatus(status -> status == NOT_FOUND, response -> Mono
            .error(new CustomNotFoundException("Debit card " + debitCard + NOT_FOUND_MESSAGE)))
        .bodyToMono(BigDecimal.class);
  }

  @Override
  public void updateAccount(String id, BigDecimal amount) {
    webClient
        .build()
        .put()
        .uri(urlAccount + "/balance/{id}/amount/{amount}", id, amount)
        .retrieve()
        .bodyToMono(Void.class)
        .subscribe();
  }
}
