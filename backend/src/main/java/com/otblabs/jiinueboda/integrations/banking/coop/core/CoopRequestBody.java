package com.otblabs.jiinueboda.integrations.banking.coop.core;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

@Data
public class CoopRequestBody {

        @JsonProperty("AcctNo")
        private String accountNumber;

        @JsonProperty("Amount")
        private String amount;

        @JsonProperty("BookedBalance")
        private String bookedBalance;

        @JsonProperty("ClearedBalance")
        private String clearedBalance;

        @JsonProperty("Currency")
        private String currency;

        @JsonProperty("CustMemoLine1")
        private String custMemoLine1;

        @JsonProperty("CustMemoLine2")
        private String custMemoLine2;

        @JsonProperty("CustMemoLine3")
        private String custMemoLine3;

        @JsonProperty("EventType")
        private String eventType;

        @JsonProperty("ExchangeRate")
        private String exchangeRate;

        @JsonProperty("Narration")
        private String narration;

        @JsonProperty("PaymentRef")
        private String paymentRef;

        @JsonProperty("PostingDate")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-ddXXX")
        private Date postingDate;

        @JsonProperty("ValueDate")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-ddXXX")
        private Date valueDate;

        @JsonProperty("TransactionDate")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-ddXXX")
        private Date transactionDate;

        @JsonProperty("TransactionId")
        private String transactionId;

    }
