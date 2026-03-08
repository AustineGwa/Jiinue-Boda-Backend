package com.otblabs.jiinueboda.callcenter;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CallComment {
    private String loanAccount;
    private String commentType;
    private boolean callPicked;
    private String clientResponse;
    private String reasonNotPicked;
    private int repId;
    private String repName;
    private String date;
    private String attachmentUrl;

}
