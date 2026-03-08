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
public class EndDayRequest {
    private BigDecimal endLat;
    private BigDecimal endLong;
    private Float endLocationAccuracy;
}
