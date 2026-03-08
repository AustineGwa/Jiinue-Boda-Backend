package com.otblabs.jiinueboda.collections.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoansByAge {
    private String id;
    private String firstName;
    private String lastName;
    private String Account;
    private int branch;
    private String phone;
    private int loanTerm;
    private int loanAge;
    private int variance;
    private int varRatio;
    private String disbursedAt;
    private String numberPlate;
}
