package com.otblabs.jiinueboda.jiinue.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewLoanRequest {
    private int userId;
    private int loanPrincipal;
    private int batteryAmount;
    private int loanTerm;
    private String loanPurpose;
    private String guarantor1Phone;
    private String guarantor2Phone;
    private int clientAsset;
    private MultipartFile loanAgreementForm;
    private int loanBranch;
    private String trackerImei;
    private String trackerSimcard;
}
