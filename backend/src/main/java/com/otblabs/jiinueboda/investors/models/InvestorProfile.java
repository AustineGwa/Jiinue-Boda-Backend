package com.otblabs.jiinueboda.investors.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InvestorProfile {
    private int id;
    private String firstName;
    private String lastName;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private int totalInvested;
    private String createdOn;
}
