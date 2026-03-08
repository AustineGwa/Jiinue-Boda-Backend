package com.otblabs.jiinueboda.users.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KycDocument {
    private int docId;
    private String docType;
    private String publicUrl;
    private String uploadedOn;
}
