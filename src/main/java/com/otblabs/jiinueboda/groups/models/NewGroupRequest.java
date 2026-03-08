package com.otblabs.jiinueboda.groups.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewGroupRequest {
    private int countyId;
    private int subCountyId;
    private int wardId;
    private String groupName;
    private double stageLat;
    private double stageLong;
    private String chairName;
    private String chairPhone;
    private String tresName;
    private String tresPhone;
    private String secName;
    private String secPhone;
    private int createdBy;
    private int expectedTotalMembers;
    private String operatingHours;
    private String accessRoads;
    private List<MultipartFile> stagePhotos;
    private String additionalNotes;
}
