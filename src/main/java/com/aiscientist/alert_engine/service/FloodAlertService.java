package com.aiscientist.alert_engine.service;

import com.aiscientist.alert_engine.dto.FloodAlertEvent;
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
public class FloodAlertService {

    private final AlertRepository alertRepository;
    private final KafkaTemplate<String, Alert> kafkaTemplate;

    public void processFloodAlert(FloodAlertEvent event) {
        // Determine severity based on flood severity
        String severity = determineSeverity(event.getFloodSeverity());
        
        Alert alert = Alert.builder()
            .alertType("FLOOD")
            .severity(severity)
            .stationId(event.getStationId())
            .stationName(event.getStationName())
            .waterLevelFeet(event.getWaterLevelFeet())
            .floodStageFeet(event.getFloodStageFeet())
            .latitude(event.getLatitude())
            .longitude(event.getLongitude())
            .description(buildDescription(event))
            .timestamp(event.getTimestamp())
            .acknowledged(false)
            .createdAt(Instant.now())
            .build();
        
        // Save to database
        alertRepository.save(alert);
        log.info("Created flood alert: {} - {} - Severity: {}", 
            event.getStationId(), event.getStationName(), event.getFloodSeverity());
        
        // Publish to Kafka based on severity
        if ("CRITICAL".equals(severity) || "MAJOR".equals(severity)) {
            kafkaTemplate.send("alerts.critical", alert);
        } else if ("MODERATE".equals(severity)) {
            kafkaTemplate.send("alerts.warning", alert);
        }
    }

    private String determineSeverity(String floodSeverity) {
        if (floodSeverity == null) {
            return "MINOR";
        }
        
        return switch (floodSeverity.toUpperCase()) {
            case "MAJOR" -> "CRITICAL";
            case "MODERATE" -> "MAJOR";
            case "MINOR" -> "MODERATE";
            case "ACTION" -> "MINOR";
            default -> "MINOR";
        };
    }

    private String buildDescription(FloodAlertEvent event) {
        StringBuilder desc = new StringBuilder();
        desc.append(String.format("Flood alert at %s (%s)", 
            event.getStationName(), event.getStationId()));
        
        if (event.getWaterLevelFeet() != null) {
            desc.append(String.format(": Water level at %.2f ft", event.getWaterLevelFeet()));
        }
        
        if (event.getFloodStageFeet() != null && event.getWaterLevelFeet() != null) {
            double aboveFloodStage = event.getWaterLevelFeet() - event.getFloodStageFeet();
            desc.append(String.format(", %.2f ft above flood stage (%.2f ft)", 
                aboveFloodStage, event.getFloodStageFeet()));
        }
        
        if (event.getFloodSeverity() != null) {
            desc.append(String.format(". Flood severity: %s", event.getFloodSeverity()));
        }
        
        if ("MAJOR".equalsIgnoreCase(event.getFloodSeverity())) {
            desc.append(". MAJOR FLOODING - Extensive property damage likely. Evacuate if instructed.");
        } else if ("MODERATE".equalsIgnoreCase(event.getFloodSeverity())) {
            desc.append(". Moderate flooding - Some property damage possible.");
        }
        
        return desc.toString();
    }
}
