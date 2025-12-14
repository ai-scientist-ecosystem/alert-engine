package com.aiscientist.alert_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for CME (Coronal Mass Ejection) events from data-collector
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmeEvent {

    private String activityId;
    private Instant startTime;
    private String sourceLocation;
    private String note;
    private String type;
    private Boolean halfAngle;
    private Double speed;
    private String latitude;
    private String longitude;
    private Double mostAccurateSpeed;
    private Instant collectedAt;
}
