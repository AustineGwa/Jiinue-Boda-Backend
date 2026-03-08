package com.otblabs.jiinueboda.integrations.banking.coop.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Source {
    @JsonProperty("AccountNumber")
    private String accountNumber;
    @JsonProperty("Amount")
    private int amount;
    @JsonProperty("TransactionCurrency")
    private String transactionCurrency;
    @JsonProperty("Narration")
    private String narration;
}