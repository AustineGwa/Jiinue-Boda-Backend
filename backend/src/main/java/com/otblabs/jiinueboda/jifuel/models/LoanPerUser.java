package com.otblabs.jiinueboda.jifuel.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoanPerUser {
    private String firstName;
    private String lastName;
    private String signupOn;
    private int totalloans;
}
