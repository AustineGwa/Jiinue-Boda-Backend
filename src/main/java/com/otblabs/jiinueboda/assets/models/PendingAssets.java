package com.otblabs.jiinueboda.assets.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingAssets {
   private int assetId;
   private int userId;
   private String make;
   private String model;
   private String chassis;
   private String engineNumber;
   private int rating;
   private int yom;
   private String color;
   private String numberPlate;
   private LocalDateTime createdAt;
}
