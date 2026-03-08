package com.otblabs.jiinueboda.recovery;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BikeRecovery {
    private Integer recoveryId;
    private int loanPrincipal;
    private String firstName;
    private String middleName;
    private String lastName;
    private int group;
    private int branch;
    private String phone;
    private String asset;
    private int loanAge;
    private String loanAccount;
    private Integer loanAgeAsAtRecoveryEntry;
    private Integer expectedAmountAsAtRecoveryEntry;
    private Integer paidAmountAsAtRecoveryEntry;
    private Integer varianceAsAtRecoveryEntry;
    private Integer varRatioAsAtRecoveryEntry;
    private String disburesementDate;
    private LocalDateTime lastPaymentDateAsAtRecoveryEntry;
    private LocalDateTime createdAt;
}