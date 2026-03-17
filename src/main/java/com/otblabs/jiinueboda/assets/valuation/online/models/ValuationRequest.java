package com.otblabs.jiinueboda.assets.valuation.online.models;



import java.time.LocalDateTime;

import lombok.*;
import org.jetbrains.annotations.NotNull;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ValuationRequest {
    private Integer assetId;
    private int technicianId;
    private Integer enginePerformance;
    private Integer gearbox;
    private Integer suspension;
    private Integer braking;
    private Integer wiringHarness;
    private Integer batteryHealth;
    private Integer chargingSystem;
    private Integer wiringNeatness;
    private Integer electricalFunc;
    private Integer gpsFeasibility;
    private Integer frontTyre;
    private Integer rearTyre;
    private Integer frameAlignment;
    private Integer fuelTank;
    private Integer bodyPanels;
    private Integer paintCondition;
    private Integer generalAppear;
    private Integer accSideMirrors;
    private Integer accCrashBars;
    private Integer accItem3;
    private Integer accItem4;
    private Integer accItem5;
    private int assignedValue;
    private String remarks;
}
