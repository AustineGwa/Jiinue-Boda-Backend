package com.otblabs.jiinueboda.assets.valuation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetValuationForm {
    private int userId;
    private int assetId;
    private String make;
    private String model;
    private String color;
    private String registration;
    private String frameNo;
    private String engineNo;
    private int yearManufacture;
    private int millage;
    private String enLeakageComment;
    private String enNoiseComment;
    private String enSmokeComment;
    private String enPowerPerformanceComment;
    private String enOtherComment;
    private String enRating;
    private String chFrameComment;
    private String chRating;
    private String bTankCom;
    private String bSeatCom;
    private String bFrontRimTyreCom;
    private String bBackRimTyreCom;
    private String bCoversCom;
    private String bBreaksCom;
    private String bRearShocksCom;
    private String bFrontShocksCom;
    private String bFootRestCom;
    private String bCrashGuardCom;
    private String bCablesCom;
    private String bDashboardCom;
    private String bCarrierCom;
    private String bStandsCom;
    private String bFendersCom;
    private String bKickStartCom;
    private String bSteeringCom;
    private String bExhaustCom;
    private String bGearLeversCom;
    private String bLeversCom;
    private String bMirrorsCom;
    private String bBodyFinalCom;
    private String bBodyRating;
    private String eBatteryStatusCom;
    private String eHornCom;
    private String eLightsCom;
    private String eStarterCom;
    private String eSignalsCom;
    private String eChargingSystemCom;
    private String eElectricFinalCom;
    private String eElectricRating;
    private String generalCom;
    private String generalRating;
    private String trackerImei;
    private String trackerSimNumber;
    private int assetTotalValue;
    private String valuerName;
    private String valuerSignature;
    private String supervisorName;
    private String supervisorSignature;
    private String createdAt;
}
