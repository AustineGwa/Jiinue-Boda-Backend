package com.otblabs.jiinueboda.callcenter;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CallCommentMultipart {
    private String loanAccount;
    private String commentType;
    private boolean callPicked;
    private boolean promiseToPay;
    private String  paymentDate;
    private String clientAttitude;
    private boolean callBackLater;
    private String  callBackDateTime;
    private String  paymentIssue;
    private String clientResponse;
    private String reasonNotPicked;
    private int repId;
    private String repName;
    private String date;
    private List<MultipartFile> proofFiles;
}
