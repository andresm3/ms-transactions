package com.bootcamp.banking.transactions.domain.dto.request;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Transfer {

  private String sender;
  private String receiver;

  private String source;
  private BigDecimal amount;

}
