package com.otblabs.jiinueboda.marketing.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarketingLead {
    private int groupId;
    private String userName;
    private String phone;
    private int channelId;
    private int campaignId;
    private int agentId;
    private int branchId;
    private String notes;
}
