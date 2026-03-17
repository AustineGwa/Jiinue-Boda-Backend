package com.otblabs.jiinueboda.loans.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanRestructureData {
    String loanID;
    int updatedLoanTerm;
}
