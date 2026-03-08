package com.otblabs.jiinueboda.assets.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValuationSubmissionData {
    int assetId;
    int evalAssignedTo;
    String evalCompletionDateTime;
    MultipartFile evaluationForm;
}
