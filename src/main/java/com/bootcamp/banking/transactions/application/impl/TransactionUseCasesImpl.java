package com.bootcamp.banking.transactions.application.impl;

import static com.bootcamp.banking.transactions.application.utils.Constants.TransactionCollection.ACCOUNT;
import static com.bootcamp.banking.transactions.application.utils.Constants.TransactionCollection.CREDIT;
import static com.bootcamp.banking.transactions.application.utils.Constants.TransactionType.ENTRY;
import static com.bootcamp.banking.transactions.application.utils.Constants.TransactionType.EXIT;

import com.bootcamp.banking.transactions.application.AccountUseCases;
import com.bootcamp.banking.transactions.application.CreditUseCases;
import com.bootcamp.banking.transactions.application.TransactionUseCases;
import com.bootcamp.banking.transactions.application.exceptions.customs.CustomInformationException;
import com.bootcamp.banking.transactions.domain.dto.request.FilterRequest;
import com.bootcamp.banking.transactions.domain.dto.request.TransactionRequest;
import com.bootcamp.banking.transactions.domain.dto.response.AccountResponse;
import com.bootcamp.banking.transactions.domain.models.Transaction;
import com.bootcamp.banking.transactions.infraestructure.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

@RequiredArgsConstructor
@Service
public class TransactionUseCasesImpl implements TransactionUseCases {

  private final TransactionRepository transactionRepository;
  private final AccountUseCases accountUseCases;
  private final CreditUseCases creditUseCases;

  private static final String SUCCESS_MESSAGE = "Successful transaction";

  @Override
  public Flux<Transaction> getTransactionsByAccountNumber(String accountNumber) {
    System.out.println(">>>getTransactionsByAccountNumber>>>>>>>>> " + accountNumber);
    return accountUseCases.findAccount(accountNumber)
        .flatMapMany(account -> transactionRepository
            .findByIdProductAndCollection(new ObjectId(account.getId()), ACCOUNT));
  }

  @Override
  public Flux<Transaction> getTransactionsByCreditNumber(String creditNumber) {
    return creditUseCases.findCredit(creditNumber)
        .flatMapMany(account -> transactionRepository
            .findByIdProductAndCollection(new ObjectId(account.getId()), CREDIT));
  }

  @Override
  public Flux<Transaction> listAccountTransactionsWithCommission(String accountNumber,
      FilterRequest request) {
    return accountUseCases.findAccount(accountNumber)
        .flatMapMany(account -> transactionRepository
            .listWithTaxByIdProductAndCollection(request.getStart(), request.getEnd(),
                account.getId(), ACCOUNT));
  }

  @Override
  public Mono<String> depositAccount(String accountNumber, TransactionRequest request) {
    return accountUseCases.findAccount(accountNumber)
        .flatMap(account -> transactionRepository
            .countByIdProductAndCollection(account.getId(), ACCOUNT)
            .flatMap(count -> {
              BigDecimal commission = getCommission(count, account);
              Transaction transaction = new Transaction(ACCOUNT, account.getId(),
                  request.getDescription(), ENTRY, request.getAmount(), commission);

              return create(transaction).flatMap(transact -> {
                updateAccountBalance(transaction, request.getAmount(), ENTRY);

                if (commission.compareTo(BigDecimal.ZERO) > 0) {
                  updateAccountBalance(transaction, commission, EXIT);
                }

                return Mono.just(SUCCESS_MESSAGE);
              });
            }));
  }

  @Override
  public Mono<String> withdrawalAccount(String accountNumber, TransactionRequest request) {
    return accountUseCases.findAccount(accountNumber)
        .flatMap(account -> transactionRepository
            .countByIdProductAndCollection(account.getId(), ACCOUNT)
            .flatMap(count -> {
              if (account.getBalance().compareTo(request.getAmount()) < 0) {
                return Mono.error(new CustomInformationException("You do not have a balance "
                    + "to carry out this transaction"));
              }

              BigDecimal commission = getCommission(count, account);
              Transaction transaction = new Transaction(ACCOUNT, account.getId(),
                  request.getDescription(), EXIT, request.getAmount(), commission);

              return create(transaction).flatMap(transact -> {
                updateAccountBalance(transaction, request.getAmount(), EXIT);

                if (commission.compareTo(BigDecimal.ZERO) > 0) {
                  updateAccountBalance(transaction, commission, EXIT);
                }

                return Mono.just(SUCCESS_MESSAGE);
              });
            }));
  }

  @Override
  public Mono<String> withdrawalFromDebitCard(String debitCard, TransactionRequest request) {
    Flux<AccountResponse> fluxAccount = accountUseCases.listByDebitCard(debitCard)
        .flatMap(this::setTotalTransactions);

    return fluxAccount
        .map(AccountResponse::getAvailableBalance)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .flatMap(total -> {
          if (total.compareTo(request.getAmount()) < 0) {
            return Mono.error(new CustomInformationException("You do not have "
                + "enough balance in your accounts"));
          }
          return Mono.just(total);
        })
        .flatMapMany(total -> fluxAccount)
        .sort(Comparator.comparing(AccountResponse::getPosition))
        .flatMap(account -> {
          if (request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal balance = account.getAvailableBalance();
            BigDecimal commission = getCommission(account.getTotalTransactions(), account);
            BigDecimal amount;
            BigDecimal amountTransaction;

            if (balance.subtract(request.getAmount()).compareTo(BigDecimal.ZERO) <= 0) {
              request.setAmount(request.getAmount().subtract(balance));
              amount = account.getBalance();
              amountTransaction = account.getAvailableBalance();
            } else {
              amount = request.getAmount().add(commission);
              amountTransaction = request.getAmount();
              request.setAmount(BigDecimal.ZERO);
            }

            Transaction transaction = new Transaction(ACCOUNT, account.getId(),
                request.getDescription(), EXIT, amountTransaction, commission);

            return create(transaction)
                .map(transact -> {
                  updateAccountBalance(transaction, amount, EXIT);

                  return account;
                });
          } else {
            return Mono.just(account);
          }
        })
        .then(Mono.just(SUCCESS_MESSAGE));
  }

  @Override
  public Mono<String> transferBetweenAccounts(String exitNumber, String entryNumber,
      TransactionRequest request) {
    Mono<AccountResponse> exitAccount = accountUseCases.findAccount(exitNumber)
        .subscribeOn(Schedulers.parallel());
    Mono<AccountResponse> entryAccount = accountUseCases.findAccount(entryNumber)
        .subscribeOn(Schedulers.parallel());

    Mono<Tuple2<AccountResponse, AccountResponse>> zip = Mono.zip(exitAccount, entryAccount);
    return zip
        .flatMap(res -> {
          System.out.println("Exit account: {}" + res.getT1());
          System.out.println("Entry account: {}" + res.getT2());
          AccountResponse acExit = res.getT1();
          AccountResponse acEntry = res.getT2();

          return transactionRepository.countByIdProductAndCollection(acExit.getId(), ACCOUNT)
              .flatMap(count -> {
                if (acExit.getBalance().compareTo(request.getAmount()) < 0) {
                  return Mono.error(new CustomInformationException("You do not have a balance "
                      + "to carry out this transaction"));
                }

                BigDecimal commission = getCommission(count, acExit);
                Transaction exit = new Transaction(ACCOUNT, acExit.getId(),
                    request.getDescription(), EXIT, request.getAmount(), commission);
                Transaction entry = new Transaction(ACCOUNT, acEntry.getId(),
                    request.getDescription(), ENTRY, request.getAmount(), commission);

                Mono<Transaction> monoExit = create(exit)
                    .doOnNext(t -> {
                      updateAccountBalance(exit, request.getAmount(), EXIT);

                      if (commission.compareTo(BigDecimal.ZERO) > 0) {
                        updateAccountBalance(exit, commission, EXIT);
                      }
                    })
                    .subscribeOn(Schedulers.parallel());
                Mono<Transaction> monoEntry = create(entry)
                    .doOnNext(t -> updateAccountBalance(entry, request.getAmount(), ENTRY))
                    .subscribeOn(Schedulers.parallel());

                return Mono.zip(monoExit, monoEntry)
                    .flatMap(t -> Mono.just(SUCCESS_MESSAGE));
              });
        });
  }

  @Override
  public Mono<String> transferAmongAccounts(String outAccountId, String inAccountId,
      BigDecimal amount, String description) {
    System.out.println(">>transferAmongAccounts>>>>>>>>>> " + outAccountId);
    Mono<AccountResponse> exitAccount = accountUseCases.findAccountById(outAccountId)
        .subscribeOn(Schedulers.parallel());
    Mono<AccountResponse> entryAccount = accountUseCases.findAccountById(inAccountId)
        .subscribeOn(Schedulers.parallel());

    Mono<Tuple2<AccountResponse, AccountResponse>> zip = Mono.zip(exitAccount, entryAccount);
    return zip
        .flatMap(res -> {
          System.out.println("Out account: {}" + res.getT1());
          System.out.println("In account: {}" + res.getT2());
          AccountResponse acExit = res.getT1();
          AccountResponse acEntry = res.getT2();

          return transactionRepository.countByIdProductAndCollection(acExit.getId(), ACCOUNT)
              .flatMap(count -> {
                if (acExit.getBalance().compareTo(amount) < 0) {
                  return Mono.error(new CustomInformationException("You do not have a balance "
                      + "to carry out this transaction"));
                }

                BigDecimal commission = getCommission(count, acExit);
                Transaction exit = new Transaction(ACCOUNT, acExit.getId(),
                    description, EXIT, amount, commission);
                Transaction entry = new Transaction(ACCOUNT, acEntry.getId(),
                    description, ENTRY, amount, commission);

                Mono<Transaction> monoExit = create(exit)
                    .doOnNext(t -> {
                      updateAccountBalance(exit, amount, EXIT);

                      if (commission.compareTo(BigDecimal.ZERO) > 0) {
                        updateAccountBalance(exit, commission, EXIT);
                      }
                    })
                    .subscribeOn(Schedulers.parallel());
                Mono<Transaction> monoEntry = create(entry)
                    .doOnNext(t -> updateAccountBalance(entry, amount, ENTRY))
                    .subscribeOn(Schedulers.parallel());

                return Mono.zip(monoExit, monoEntry)
                    .flatMap(t -> Mono.just(SUCCESS_MESSAGE));
              });
        });
  }
  @Override
  public Mono<String> payCredit(String creditNumber, BigDecimal amount) {

    return creditUseCases.findCredit(creditNumber)
        .flatMap(account -> {
          Transaction transaction = new Transaction();
          transaction.setIdProduct(new ObjectId(account.getId()));
          transaction.setCollection(CREDIT);
          transaction.setType(ENTRY);
          transaction.setDate(LocalDateTime.now());
          transaction.setAmount(amount);
          create(transaction);
          updateCreditBalance(transaction, amount, ENTRY);

          return Mono.just(SUCCESS_MESSAGE);
        });
  }

  @Override
  public Mono<String> spendCredit(String creditNumber, BigDecimal amount) {

    return creditUseCases.findCredit(creditNumber)
        .flatMap(account -> {
          Transaction transaction = new Transaction();
          transaction.setIdProduct(new ObjectId(account.getId()));
          transaction.setCollection(CREDIT);
          transaction.setType(EXIT);
          transaction.setDate(LocalDateTime.now());
          transaction.setAmount(amount);
          create(transaction);
          updateCreditBalance(transaction, amount, EXIT);

          return Mono.just(SUCCESS_MESSAGE);
        });
  }


  private BigDecimal getCommission(Long count, AccountResponse account) {
    boolean requireCommission = account.getTypeAccount().getMaxTransactions() != null
        && count >= account.getTypeAccount().getMaxTransactions();
    if (!requireCommission) {
      return BigDecimal.ZERO;
    }

    return account.getTypeAccount().getCommission() == null
        ? BigDecimal.ZERO :
        account.getTypeAccount().getCommission();
  }

  private Mono<Transaction> create(Transaction transaction) {
    return transactionRepository.save(transaction)
        .flatMap(x -> {
          System.out.println("Created a new transaction with id = {}" + x.getId());
          return Mono.just(x);
        });
  }

  private void updateAccountBalance(Transaction transaction, BigDecimal amount, int type) {
    System.out.println("updateAccountBalance>>" + amount);
    BigDecimal finalAmount = type == ENTRY ? amount : amount.multiply(BigDecimal.valueOf(-1));
    System.out.println("finalAmount>>" + finalAmount);
    accountUseCases
        .updateAccount(transaction.getIdProduct().toString(), finalAmount);
  }

  private void updateCreditBalance(Transaction transaction, BigDecimal amount, int type) {
    BigDecimal finalAmount = type == ENTRY ? amount : amount.multiply(BigDecimal.valueOf(-1));
    System.out.println(">>>updateCreditBalance>finalAmount>> " + finalAmount);
    creditUseCases
        .updateCredit(transaction.getIdProduct().toString(), finalAmount);
  }

  private Mono<AccountResponse> setTotalTransactions(AccountResponse account) {
    return transactionRepository
        .countByIdProductAndCollection(account.getId(), ACCOUNT)
        .flatMap(count -> {
          BigDecimal commission = account.getTypeAccount().getCommission() == null
              ? BigDecimal.ZERO :
              account.getTypeAccount().getCommission();
          BigDecimal availableBalance = count >= account.getTypeAccount().getMaxTransactions()
              ? account.getBalance().subtract(commission) :
              account.getBalance();
          account.setAvailableBalance(availableBalance);
          account.setTotalTransactions(count);

          return Mono.just(account);
        });
  }
}
