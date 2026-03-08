package com.otblabs.jiinueboda.accounting.expenses.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCategory {
    private int id;
    private String name;
    private String description;
}
