package com.otblabs.jiinueboda.dashboard.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyPaymentTracker {
    String date;
    int dailyAmountExpected;
    int  amountPaid;

}
