package com.otblabs.jiinueboda.integrations.momo.pesapal.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PesapalError {
    private String type;
    private String code;
    private String message;
}
