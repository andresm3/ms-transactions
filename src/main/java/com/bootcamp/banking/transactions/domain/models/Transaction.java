package com.bootcamp.banking.transactions.domain.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
@Document("transactions")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transaction {

  private String id;
//  private String operationType;
//  private String operationCode;
//  private String productId;
//  private Double amount;
//  private Date date;
  private int collection;
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId idProduct;
  private String description;
  private int type;
  @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
  private LocalDateTime date;
  private int month;
  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal amount;
  @Field(targetType = FieldType.DECIMAL128)
  private BigDecimal commission;

  public Transaction(int collection, String idProduct,
      String description, int type,
      BigDecimal amount, BigDecimal commission) {
    this.collection = collection;
    this.idProduct = new ObjectId(idProduct);
    this.description = description;
    this.type = type;
    this.date = LocalDateTime.now();
    this.month = LocalDate.now().getMonthValue();
    this.amount = amount;
    this.commission = commission;
  }
}
