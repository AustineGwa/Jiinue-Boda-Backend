package com.otblabs.jiinueboda.assets.valuation.online.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OnlineAssetValuation {
    private Integer id;
    private Integer assetId;
    private int  technicianId;

    // Section 2 – Wiring & GPS
    private Integer wiringHarness;
    private Integer batteryHealth;
    private Integer chargingSystem;
    private Integer wiringNeatness;
    private Integer electricalFunc;
    private Integer gpsFeasibility;
    private BigDecimal wiringScore;

    // Section 3 – Tyres
    private Integer frontTyre;
    private Integer rearTyre;
    private BigDecimal tyreScore;

    // Section 4 – Body & Frame
    private Integer frameAlignment;
    private Integer fuelTank;
    private Integer bodyPanels;
    private Integer paintCondition;
    private Integer generalAppear;
    private BigDecimal bodyScore;

    // Section 5 – Accessories
    private Integer accSideMirrors;
    private Integer accCrashBars;
    private Integer accItem3;
    private Integer accItem4;
    private Integer accItem5;
    private BigDecimal accessoryScore;

    // Totals
    private BigDecimal totalScore;
    private String     grade;
    private String     remarks;
    private int        assignedValue;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


