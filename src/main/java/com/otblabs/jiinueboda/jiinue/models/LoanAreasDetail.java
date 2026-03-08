package com.otblabs.jiinueboda.jiinue.models;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoanAreasDetail {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phone;
    private int appId;
    private String paybill;
    private String loanId;
    private int clientLoanTotal;
    private int totalPaid;
    private int totalExpectedByToday;
    private int currentAreas;
    private int dailyAmountExpected;
    private int averageDailyPayment;
    private int loanTerm;
    private Date issuedOn;
    private int loanAgeTodate;
}
