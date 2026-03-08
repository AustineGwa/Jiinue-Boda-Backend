package com.otblabs.jiinueboda.jiinue.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanRequestResponse {
    String message;
    boolean successful;
    Loan loan;
}
