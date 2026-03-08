package com.otblabs.jiinueboda.assets.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AssetAttachment {
    private int docId;
    private String docType;
    private String publicUrl;
    private String uploadedOn;
}
