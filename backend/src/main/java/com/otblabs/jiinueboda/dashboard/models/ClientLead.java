package com.otblabs.jiinueboda.dashboard.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientLead {
    private int id;
    private int groupId;
    private String userName;
    private String phoneNumber;
    private String marketingStage;
    private String createdBy;
    private String createdAt;

}
