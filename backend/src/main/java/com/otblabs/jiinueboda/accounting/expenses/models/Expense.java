package com.otblabs.jiinueboda.accounting.expenses.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    private int  amount;
    private int expenseCategoryId;
    private String expenseCategoryName;
    private String description;
    private String transactionRef;
    private String disbursedAt;
}