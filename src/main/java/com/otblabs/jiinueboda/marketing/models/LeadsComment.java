package com.otblabs.jiinueboda.marketing.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeadsComment {
    private int leadId;
    private String comment;
    private String calledBy;
    private String createdAt;
}
