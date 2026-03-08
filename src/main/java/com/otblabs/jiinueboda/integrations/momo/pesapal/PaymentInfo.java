package com.otblabs.jiinueboda.integrations.momo.pesapal;


import com.otblabs.jiinueboda.integrations.momo.pesapal.models.BillingAddress;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentInfo {
    private String id;
    private String currency;
    private double amount;
    private String description;
    private String callbackUrl;
    private String redirectMode;
    private String notificationId;
    private String branch;
    private BillingAddress billingAddress;
}






