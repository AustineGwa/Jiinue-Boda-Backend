package com.otblabs.jiinueboda.jiinue.models;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoanApprovalRequest {
    private String loanId;
    private String approvalLevel;
    private String updatedStatus;
    private String approvalComment;
}
