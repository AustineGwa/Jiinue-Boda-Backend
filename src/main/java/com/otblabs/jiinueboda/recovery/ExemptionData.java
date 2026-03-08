package com.otblabs.jiinueboda.recovery;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExemptionData {
    private int recoveryId;
    private int daysExempted;
    private String reason;
    private int userId;
}
