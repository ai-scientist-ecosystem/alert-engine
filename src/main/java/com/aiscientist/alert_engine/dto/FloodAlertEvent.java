package com.aiscientist.alert_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for water level and flood events from data-collector
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloodAlertEvent {

    private String stationId;
    private String stationName;
    private String source;
    private String locationType;
    private Double latitude;
    private Double longitude;
    private Instant timestamp;
    private Double waterLevelMeters;
    private Double waterLevelFeet;
    private String datum;
    private Double dischargeCfs;
    private Double gageHeightFeet;
    private Double floodStageFeet;
    private String floodSeverity;
    private boolean isFlooding;
    private String qualityCode;
}
