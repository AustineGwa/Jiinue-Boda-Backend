package com.otblabs.jiinueboda.accounting.hr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    private int userId;
    private String fullName;
    private String mpesaAccount;
    private String bankAccount;
    private double salary;
    private double advancesThisMonth;
    private double overdraftThisMonth;
    private String lastPaymentDate;

}
