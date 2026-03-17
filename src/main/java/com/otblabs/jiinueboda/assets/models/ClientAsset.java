package com.otblabs.jiinueboda.assets.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientAsset {
    private int id;
    private String  brand;
    private String make;
    private String model;
    private String lplate;
    private String chassis;
    private String engineNumber;
    private int rating;
    private int yom;
    private String color;
    private String odometer;
    private String acondition;
    private String userId;
    private int evalStatus;
    private String evalAssignedTo;
    private String evalReqDate;
    private String evalCompDate;
    private String evaluationReport;
    private String chargedLogBook;
}
