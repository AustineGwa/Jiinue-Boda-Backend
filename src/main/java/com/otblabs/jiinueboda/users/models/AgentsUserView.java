package com.otblabs.jiinueboda.users.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AgentsUserView {
    private int userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String createdAt;
    private String referredBy;
    private String county;
    private String subCounty;
    private String ward;
    private String stageName;
    private int stageId;
    private String loanStatus;

}

