package com.otblabs.jiinueboda.accounting.expenses.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PendingExpense {
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
