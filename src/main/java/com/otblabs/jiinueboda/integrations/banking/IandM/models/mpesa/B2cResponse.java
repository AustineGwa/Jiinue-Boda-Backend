package com.otblabs.jiinueboda.integrations.banking.IandM.models.mpesa;

import lombok.Data;

@Data
public class B2cResponse {
    private int approvalCode;
    private String responseCode;
    private String responseMessage;
    private String targetTranID;
    private String targetRefNumber;
}