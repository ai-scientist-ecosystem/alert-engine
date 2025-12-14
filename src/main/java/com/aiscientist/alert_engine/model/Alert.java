package com.aiscientist.alert_engine.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_severity", columnList = "severity"),
    @Index(name = "idx_alert_timestamp", columnList = "timestamp"),
    @Index(name = "idx_alert_type", columnList = "alert_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;
    
    @Column(name = "severity", nullable = false, length = 20)
    private String severity;
    
    @Column(name = "kp_value")
    private Double kpValue;
    
    // Earthquake-specific fields
    @Column(name = "earthquake_id")
    private String earthquakeId;
    
    @Column(name = "magnitude")
    private Double magnitude;
    
    @Column(name = "depth_km")
    private Double depthKm;
    
    @Column(name = "location", length = 500)
    private String location;
    
    @Column(name = "region")
    private String region;
    
    // Tsunami-specific fields
    @Column(name = "tsunami_risk_score")
    private Integer tsunamiRiskScore;
    
    // Flood-specific fields
    @Column(name = "station_id")
    private String stationId;
    
    @Column(name = "station_name")
    private String stationName;
    
    @Column(name = "water_level_feet")
    private Double waterLevelFeet;
    
    @Column(name = "flood_stage_feet")
    private Double floodStageFeet;
    
    // CME-specific fields
    @Column(name = "cme_speed")
    private Double cmeSpeed;
    
    @Column(name = "cme_type")
    private String cmeType;
    
    // Geographic coordinates
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    
    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "acknowledged", nullable = false)
    private Boolean acknowledged = false;
    
    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (acknowledged == null) {
            acknowledged = false;
        }
    }
}
