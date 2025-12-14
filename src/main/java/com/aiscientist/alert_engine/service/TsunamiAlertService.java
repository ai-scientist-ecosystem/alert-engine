package com.aiscientist.alert_engine.service;

import com.aiscientist.alert_engine.dto.EarthquakeEvent;
import com.aiscientist.alert_engine.model.Alert;
import com.aiscientist.alert_engine.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class TsunamiAlertService {

    private final AlertRepository alertRepository;
    private final KafkaTemplate<String, Alert> kafkaTemplate;

    public void processTsunamiWarning(EarthquakeEvent event) {
        // Determine severity based on tsunami risk score
        String severity = determineSeverity(event.getTsunamiRiskScore());
        
        Alert alert = Alert.builder()
            .alertType("TSUNAMI")
            .severity(severity)
            .earthquakeId(event.getEarthquakeId())
            .magnitude(event.getMagnitude())
            .tsunamiRiskScore(event.getTsunamiRiskScore())
            .location(event.getLocation())
            .region(event.getRegion())
            .latitude(event.getLatitude())
            .longitude(event.getLongitude())
            .description(buildDescription(event))
            .timestamp(event.getEventTime())
            .acknowledged(false)
            .createdAt(Instant.now())
            .build();
        
        // Save to database
        alertRepository.save(alert);
        log.warn("Created tsunami alert: {} - Risk Score: {}, Location: {}", 
            event.getEarthquakeId(), event.getTsunamiRiskScore(), event.getLocation());
        
        // Always publish tsunami warnings to critical topic
        kafkaTemplate.send("alerts.critical", alert);
    }

    private String determineSeverity(Integer riskScore) {
        if (riskScore == null) {
            return "CRITICAL";
        }
        
        if (riskScore >= 70) {
            return "EXTREME"; // Very high tsunami risk
        } else if (riskScore >= 50) {
            return "CRITICAL"; // High tsunami risk
        } else if (riskScore >= 30) {
            return "MAJOR"; // Moderate tsunami risk
        } else {
            return "MODERATE"; // Low tsunami risk
        }
    }

    private String buildDescription(EarthquakeEvent event) {
        StringBuilder desc = new StringBuilder();
        desc.append(String.format("TSUNAMI WARNING: Magnitude %.1f earthquake at %s", 
            event.getMagnitude(), event.getLocation()));
        
        if (event.getTsunamiRiskScore() != null) {
            desc.append(String.format(" with tsunami risk score of %d", event.getTsunamiRiskScore()));
        }
        
        desc.append(". Coastal areas should prepare for potential tsunami waves.");
        
        if (event.getTsunamiRiskScore() != null && event.getTsunamiRiskScore() >= 70) {
            desc.append(" IMMEDIATE EVACUATION RECOMMENDED for coastal communities.");
        }
        
        return desc.toString();
    }
}
