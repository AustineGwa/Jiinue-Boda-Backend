package com.otblabs.jiinueboda.integrations.banking.IandM;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IMMpesapushResult {
    private int statusCode;
    private String statusDescription;
    private String mpesaRef;
}
