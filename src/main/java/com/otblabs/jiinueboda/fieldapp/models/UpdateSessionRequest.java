package com.otblabs.jiinueboda.fieldapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSessionRequest {
    private String networkStatus;
    private Integer batteryLevel;
    private String weatherConditions;
    private Float temperature;
}

