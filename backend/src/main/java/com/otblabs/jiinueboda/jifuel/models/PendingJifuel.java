package com.otblabs.jiinueboda.jifuel.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PendingJifuel {
    private String firstName;
    private String middleName;
    private String lastName;
    private String phone;
    private int appId;
    private String loanId;
    private int loanPrincipal;
}
