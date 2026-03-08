package com.otblabs.jiinueboda.assets.models;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewAssetDto {
    private int userId;
    private String brand;
    private String make;
    private String model;
    private String Lplate;
    private String chassis;
    private String engineNumber;
    private int rating;
    private int yom;
    private String color;
    private String odometer;
    private String Acondition;
}
