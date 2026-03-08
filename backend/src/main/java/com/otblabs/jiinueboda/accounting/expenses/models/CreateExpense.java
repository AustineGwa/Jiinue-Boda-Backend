package com.otblabs.jiinueboda.accounting.expenses.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateExpense {
    private int amount;
    private int mainMainCategoryId;
    private int subCategoryId;
    private int minorSubcategoryId;
    private String description;
    private RecieverType recieverType;
    private String reciever;
    private String accountNumber;
}


