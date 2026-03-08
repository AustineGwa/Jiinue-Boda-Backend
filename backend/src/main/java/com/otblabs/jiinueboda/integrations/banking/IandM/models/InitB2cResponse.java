package com.otblabs.jiinueboda.integrations.banking.IandM.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitB2cResponse {
    String approvalCode;
    String responseCode;
    String responseMessage;
    String targetTranID;
    String targetRefNumber;
}

//{"approvalCode": "0","responseCode": "ACCEPTED","responseMessage": "Accept the service request successfully.","targetTranID": "S452801","targetRefNumber": "511382_IMBANK_PG_200814000140_200814008810"}
//{ "approvalCode": "", "responseCode": "FAILED", "responseMessage": "Invalid Request Reference Number - Length must be 12 digits", "targetTranID": "", "targetRefNumber": "", "paymentSystemRefNumber": "", "targetResponse": "" }
//{ "approvalCode": "", "responseCode": "FAILED", "responseMessage": "Request Not Permitted.", "targetTranID": "", "targetRefNumber": "", "paymentSystemRefNumber": "", "targetResponse": "" }

