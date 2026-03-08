package com.otblabs.jiinueboda.integrations.momo.pesapal.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PesapalAuthResponse {
    private String token;
    private String expiryDate;
    private String error;
    private String status;
    private String message;
}
