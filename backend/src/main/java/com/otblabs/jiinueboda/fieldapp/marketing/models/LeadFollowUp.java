package com.otblabs.jiinueboda.fieldapp.marketing.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeadFollowUp {
    private int leadId;
    private String comment;
    private int calledBy;
}
