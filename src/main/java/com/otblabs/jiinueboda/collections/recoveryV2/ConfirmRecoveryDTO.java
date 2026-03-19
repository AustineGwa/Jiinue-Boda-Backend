package com.otblabs.jiinueboda.collections.recoveryV2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmRecoveryDTO {
    private int recoveryAmount;
    private String recoveryComment;
    private String loanAccount;
}