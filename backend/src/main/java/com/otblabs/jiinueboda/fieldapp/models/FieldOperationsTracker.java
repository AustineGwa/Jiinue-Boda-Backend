package com.otblabs.jiinueboda.fieldapp.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("field_operations_tracker")
public class FieldOperationsTracker {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("session_date")
    private LocalDate sessionDate;

    @Column("start_time")
    private LocalDateTime startTime;

    @Column("end_time")
    private LocalDateTime endTime;

    @Column("session_status")
    private String sessionStatus;

    @Column("start_lat")
    private BigDecimal startLat;

    @Column("start_lng")
    private BigDecimal startLng;

    @Column("start_location_accuracy")
    private Float startLocationAccuracy;

    @Column("end_lat")
    private BigDecimal endLat;

    @Column("end_lng")
    private BigDecimal endLng;

    @Column("end_location_accuracy")
    private Float endLocationAccuracy;

    @Column("work_county")
    private String workCounty;

    @Column("work_sub_county")
    private String workSubCounty;

    @Column("work_ward")
    private String workWard;

    @Column("work_area")
    private String workArea;

    @Column("work_stage")
    private String workStage;

    @Column("activity")
    private String activity;

    @Column("device_info")
    private String deviceInfo;

    @Column("network_status")
    private String networkStatus;

    @Column("battery_level")
    private Integer batteryLevel;

    @Column("app_version")
    private String appVersion;

    @Column("weather_conditions")
    private String weatherConditions;

    @Column("temperature")
    private Float temperature;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
