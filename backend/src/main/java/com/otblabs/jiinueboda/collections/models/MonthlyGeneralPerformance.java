package com.otblabs.jiinueboda.collections.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyGeneralPerformance {
    private int expectedAmount;
    private int totalCollected;
    private int totalDisbursed;
    private int totalPrincipal;
}
