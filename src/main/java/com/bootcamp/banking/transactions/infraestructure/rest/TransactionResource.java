package com.bootcamp.banking.transactions.infraestructure.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

import com.bootcamp.banking.transactions.domain.dto.request.FilterRequest;
import com.bootcamp.banking.transactions.domain.dto.request.TransactionRequest;
import com.bootcamp.banking.transactions.domain.dto.request.Transfer;
import com.bootcamp.banking.transactions.domain.models.Transaction;
import com.bootcamp.banking.transactions.application.TransactionUseCases;
import java.io.IOException;
import java.math.BigDecimal;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionResource {

  private final TransactionUseCases transactionUseCases;

  @GetMapping(value = "/account/{number}"
//      , produces = TEXT_EVENT_STREAM_VALUE
  )
  public Flux<Transaction> findTxAccountByAccountNumber(@PathVariable String number) {
    return transactionUseCases.getTransactionsByAccountNumber(number);
  }

  @GetMapping(value = "/credit/{number}"
//      , produces = TEXT_EVENT_STREAM_VALUE
  )
  public Flux<Transaction> findTxCreditByCreditId(@PathVariable String number) {
    return transactionUseCases.getTransactionsByCreditNumber(number);
  }

  @GetMapping(value = "/account/{number}/commissions"
//      , produces = TEXT_EVENT_STREAM_VALUE
  )
  public Flux<Transaction> listTxWithCommissionByAccountNumber(@PathVariable String number,
      FilterRequest request) {
    return transactionUseCases.listAccountTransactionsWithCommission(number, request);
  }

  /**
   * Deposit account.
   */
  @PostMapping("/deposit/account/{number}")
  @ResponseStatus(CREATED)
  public Mono<String> depositAccount(@PathVariable String number,
      @Valid @RequestBody TransactionRequest request) {
    if (StringUtils.isBlank(request.getDescription())) {
      request.setDescription("Ingreso en efectivo");
    }
    return transactionUseCases.depositAccount(number, request);
  }

  /**
   * Withdraw from account.
   */
  @PostMapping("/withdrawal/account/{number}")
  @ResponseStatus(CREATED)
  public Mono<String> withdrawalAccount(@PathVariable String number,
      @Valid @RequestBody TransactionRequest request) {
    if (StringUtils.isBlank(request.getDescription())) {
      request.setDescription("Retiro de efectivo");
    }
    return transactionUseCases.withdrawalAccount(number, request);
  }

  /**
   * Withdraw from debit card.
   */
  @PostMapping("/withdrawal/debitCard/{debitCard}")
  @ResponseStatus(CREATED)
  public Mono<String> withdrawalFromDebitCard(@PathVariable String debitCard,
      @Valid @RequestBody TransactionRequest request) {
    if (StringUtils.isBlank(request.getDescription())) {
      request.setDescription("Retiro de efectivo");
    }
    return transactionUseCases.withdrawalFromDebitCard(debitCard, request);
  }

  /**
   * Transfer between two accounts.
   */
  @PostMapping("/transfer/account/{exitNumber}/account/{entryNumber}")
  @ResponseStatus(CREATED)
  public Mono<String> transferBetweenAccounts(@PathVariable String exitNumber,
      @PathVariable String entryNumber,
      @Valid @RequestBody TransactionRequest request) {
    if (StringUtils.isBlank(request.getDescription())) {
      request.setDescription("Transferencia entre cuentas");
    }
    return transactionUseCases.transferBetweenAccounts(exitNumber, entryNumber, request);
  }

  @KafkaListener(topics = "BOOTCOIN-EXCHANGE", groupId = "group_id", containerFactory = "transferListener")
  @ResponseStatus(CREATED)
  public Mono<String> transferAmongAccountsEvent(Transfer transfer) {
    System.out.println(">>>>>>" + transfer.toString());
    return transactionUseCases.transferAmongAccounts(transfer.getSender(), transfer.getReceiver(),
        transfer.getAmount(), transfer.getSource());
  }

  @PostMapping("/pay/credit/{number}/{amount}")
  @ResponseStatus(CREATED)
  public Mono<String> payCredit(@PathVariable String number,
      @PathVariable BigDecimal amount) {
    System.out.println(">>>>>payCredit>>>amount> " + amount);
    return transactionUseCases.payCredit(number, amount);
  }

  @PostMapping("/spend/credit/{number}/{amount}")
  @ResponseStatus(CREATED)
  public Mono<String> spendCredit(@PathVariable String number,
      @PathVariable BigDecimal amount) {
    BigDecimal finalAmount = amount.multiply(BigDecimal.valueOf(-1));
    return transactionUseCases.spendCredit(number, finalAmount);
  }
}
