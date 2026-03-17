package com.otblabs.jiinueboda.marketing.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarketingPerformanceDTO {
    private String channelName;
    private int totalLeads;
    private int applicationsStarted;
    private int applicationsSubmitted;
    private int loansApproved;
    private int loansDisbursed;
    private double conversionToDisbursed;
}
