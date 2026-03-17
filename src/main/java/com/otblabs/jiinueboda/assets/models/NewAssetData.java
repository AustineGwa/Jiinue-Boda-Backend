package com.otblabs.jiinueboda.assets.models;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewAssetData {
    private int userId;
    private String brand;
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
    private MultipartFile chargedLogBook;
    private List<MultipartFile> assetImages;
}




