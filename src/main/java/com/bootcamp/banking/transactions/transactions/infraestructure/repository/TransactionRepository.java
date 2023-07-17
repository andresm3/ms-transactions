package com.bootcamp.banking.transactions.transactions.infraestructure.repository;

import com.bootcamp.banking.transactions.transactions.domain.models.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {

//  Flux<Transaction> findByClientId(String clientId);
//
//  Flux<Transaction> findByClientIdAndProductType(String clientId, String productType);

}
