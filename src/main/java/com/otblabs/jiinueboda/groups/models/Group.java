package com.otblabs.jiinueboda.groups.models;

import lombok.Data;

import java.util.List;

@Data
public class Group {
    private int id;
    private String groupName;
    private String county;
    private String subCounty;
    private String ward;
    private double stageLat;
    private double stageLong;
    private String chairName;
    private String chairPhone;
    private String tresName;
    private String tresPhone;
    private String secName;
    private String secPhone;
    private int totalMembers;
    private boolean locationDataUpdated;
    private String createdAt;
    private String createdBy;
    private String operatingHours;
    private String accessRoads;
    private List<String> stagePhotos;
    private String additionalNotes;

}
