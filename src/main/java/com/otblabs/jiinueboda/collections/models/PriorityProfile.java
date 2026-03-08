package com.otblabs.jiinueboda.collections.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PriorityProfile {
    String loanDisbursedAt;
    String account;
    String firstName;
    String lastName;
    String phone;
    String branch;
    int loanTerm;
    int loanAge;
    int variance;
    int varRatio;
    int numberOfCalls;
}
