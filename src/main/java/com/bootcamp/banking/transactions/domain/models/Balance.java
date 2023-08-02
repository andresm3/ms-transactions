package com.bootcamp.banking.transactions.domain.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("balances")
public class Balance {

  private String id;
  private String clientId;
  private String productId;
  private String productType;
  private Double balanceCalculated;
}
