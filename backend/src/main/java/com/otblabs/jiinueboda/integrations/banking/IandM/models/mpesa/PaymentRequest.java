package com.otblabs.jiinueboda.integrations.banking.IandM.models.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentRequest {
    private Sender sender;
    @JsonProperty("trandetails")
    private TranDetails tranDetails;
    @JsonProperty("mobilemoneypayment")
    private MobileMoneyPayment mobileMoneyPayment;
}
