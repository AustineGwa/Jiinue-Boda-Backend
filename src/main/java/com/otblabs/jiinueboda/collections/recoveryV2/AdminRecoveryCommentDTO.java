package com.otblabs.jiinueboda.collections.recoveryV2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminRecoveryCommentDTO {
    private boolean adminApproval;
    private String adminComment;
    private String loanAccount;
}
