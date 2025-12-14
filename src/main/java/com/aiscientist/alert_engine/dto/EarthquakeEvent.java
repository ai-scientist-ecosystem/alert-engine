package com.aiscientist.alert_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for earthquake events from data-collector
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarthquakeEvent {

    private String earthquakeId;
    private Double magnitude;
    private String magnitudeType;
    private Double depthKm;
    private Double latitude;
    private Double longitude;
    private Instant eventTime;
    private String location;
    private String region;
    private String severity;
    private Boolean dangerous;
    private Boolean catastrophic;
    private Boolean shallow;
    private Boolean tsunamiWarning;
    private Integer tsunamiRiskScore;
    private String alertLevel;
    private Integer significance;
    private Integer feltReports;
    private String dataSource;
    private String eventUrl;
    private Instant collectedAt;
    private String eventType;
}
