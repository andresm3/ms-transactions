package com.bootcamp.banking.transactions.infraestructure.repository;

import com.bootcamp.banking.transactions.domain.models.Transaction;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Transaction repository.
 */
@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, ObjectId>,
    CustomTransactionRepository {
  Flux<Transaction> findByIdProductAndCollection(ObjectId idProduct, int collection);
}
