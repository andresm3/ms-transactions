package com.bootcamp.banking.transactions.transactions.domain.models;

import java.util.Date;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("transactions")
public class Transaction {

  private String id;
  private String operationType;
  private String operationCode;
  private String productId;
  private Double amount;
  private Date date;

}
