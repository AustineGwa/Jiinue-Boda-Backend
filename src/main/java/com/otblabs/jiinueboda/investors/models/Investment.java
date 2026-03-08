package com.otblabs.jiinueboda.investors.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Investment {
    private int id;
    private int amountInvested;
    private double interestPercentage;
    private boolean investmentStatus;
    private String createdAt;

}
