package com.otblabs.jiinueboda.fieldapp.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartDayRequest {
    private BigDecimal startLat;
    private BigDecimal startLng;
    private Float startLocationAccuracy;
    private String deviceInfo;
    private String networkStatus;
    private Integer batteryLevel;
    private String appVersion;
    private String weatherConditions;
    private Float temperature;
    private String workCounty;
    private String workSubCounty;
    private String workWard;
    private String workArea;
    private String workStage;
    private String activity;
}
