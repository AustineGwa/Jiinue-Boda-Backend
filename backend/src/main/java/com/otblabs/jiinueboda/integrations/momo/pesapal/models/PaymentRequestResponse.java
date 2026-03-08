package com.otblabs.jiinueboda.integrations.momo.pesapal.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaymentRequestResponse {
    private String orderTrackingId;
    private String merchantReference;
    private String redirectUrl;
    private String error;
    private String status;
}

