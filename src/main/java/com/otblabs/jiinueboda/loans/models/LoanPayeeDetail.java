package com.otblabs.jiinueboda.loans.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanPayeeDetail {
    private String partyB;
    private String occasion;
    private int appId;
    private int shotcode;
    private int dailyPayment;
}
