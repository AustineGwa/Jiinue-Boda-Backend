package com.otblabs.jiinueboda.accounting.expenses.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProccessedExpense {
    private int id;
    private int amount;
    private String description;
    private RecieverType recieverType;
    private String reciever;
    private String accountNumber;
    private String createdAt;
    private String mainCategory;
    private String subCategory;
    private String minorSubCategory;
    private String mpesaRefferenceCode;
}
