package com.otblabs.jiinueboda.assets.valuation.online.models;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Inbound DTO from the valuation form.
 * Scores (wiring_score, tyre_score, etc.) are NOT accepted from the client —
 * they are computed server-side in the service layer.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValuationRequest {

    @NotNull
    private Integer assetId;

    @NotBlank(message = "inspector is required")
    @Size(max = 100)
    private String inspector;

    @NotNull
    private LocalDate inspectionDate;

    // ── Section 2 – Wiring & GPS (0–3)
    @Min(0) @Max(3) private Integer wiringHarness;
    @Min(0) @Max(3) private Integer batteryHealth;
    @Min(0) @Max(3) private Integer chargingSystem;
    @Min(0) @Max(3) private Integer wiringNeatness;
    @Min(0) @Max(3) private Integer electricalFunc;
    @Min(0) @Max(3) private Integer gpsFeasibility;

    // ── Section 3 – Tyres (0–3)
    @Min(0) @Max(3) private Integer frontTyre;
    @Min(0) @Max(3) private Integer rearTyre;

    // ── Section 4 – Body & Frame (0–3)
    @Min(0) @Max(3) private Integer frameAlignment;
    @Min(0) @Max(3) private Integer fuelTank;
    @Min(0) @Max(3) private Integer bodyPanels;
    @Min(0) @Max(3) private Integer paintCondition;
    @Min(0) @Max(3) private Integer generalAppear;

    // ── Section 5 – Accessories (0–2)
    @Min(0) @Max(2) private Integer accSideMirrors;
    @Min(0) @Max(2) private Integer accCrashBars;
    @Min(0) @Max(2) private Integer accItem3;
    @Min(0) @Max(2) private Integer accItem4;
    @Min(0) @Max(2) private Integer accItem5;

    private String remarks;
}
