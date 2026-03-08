package com.otblabs.jiinueboda.fieldapp.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PauseSessionRequest {

//    @NotNull(message = "Current latitude is required")
    private BigDecimal currentLat;

//    @NotNull(message = "Current longitude is required")
    private BigDecimal currentLng;

    private Float locationAccuracy;
}
