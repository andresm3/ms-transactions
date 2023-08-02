package com.bootcamp.banking.transactions.infraestructure.repository;

import com.bootcamp.banking.transactions.domain.models.Transaction;
import java.time.LocalDate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Custom transaction repository.
 */
public interface CustomTransactionRepository {
  Mono<Long> countByIdProductAndCollection(String idProduct, Integer collection);

  Flux<Transaction> listWithTaxByIdProductAndCollection(LocalDate start, LocalDate end,
                                                        String idProduct, int collection);
}
