package com.otblabs.jiinueboda.integrations.banking.IandM.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MpesaBankRequestData {
    private String transRef;
    private String mpesaRef;
    private String MSISDN;
    private double amount;
    private String transactiondate;
    private String narration;
    private String accountRef;
    private String sortCode;
}

