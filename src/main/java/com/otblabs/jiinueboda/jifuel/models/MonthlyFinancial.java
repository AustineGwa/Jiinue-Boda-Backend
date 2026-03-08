package com.otblabs.jiinueboda.jifuel.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MonthlyFinancial {
    private String month;
    private int totalDisbursed;
    private int totalRepaid;
}
