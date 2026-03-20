package com.otblabs.jiinueboda.collections.recoveryV2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetReleaseDTO {

    private String loanAccount;
    private String releaseType;
    private String releaseNotes;
    private Double saleAmount;

    private Checklist checklist;
    private ChecklistProofs checklistProofs;

    private String opsManagerName;
    private String opsManagerComment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Checklist {
        private Boolean loanClearance;
        private Boolean saleAgreement;
        private Boolean recoveryFeesPaid;
        private Boolean storageFeesCleared;
        private Boolean trackerCleared;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChecklistProofs {
        private String loanClearance;
        private String saleAgreement;
        private String recoveryFeesPaid;
        private String storageFeesCleared;
        private String trackerCleared;
    }
}