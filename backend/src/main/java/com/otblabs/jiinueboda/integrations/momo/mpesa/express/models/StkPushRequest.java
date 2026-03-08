package com.otblabs.jiinueboda.integrations.momo.mpesa.express.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StkPushRequest {
    private String userId;
    private String loanId;
    private String amount;
    private String phoneNumber;
    private String transactionDesc;
}
