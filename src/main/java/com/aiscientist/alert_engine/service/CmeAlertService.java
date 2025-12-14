package com.aiscientist.alert_engine.service;

import com.aiscientist.alert_engine.dto.CmeEvent;
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
public class CmeAlertService {

    private final AlertRepository alertRepository;
    private final KafkaTemplate<String, Alert> kafkaTemplate;

    public void processCmeEvent(CmeEvent event) {
        // Determine severity based on CME speed
        Double speed = event.getMostAccurateSpeed() != null ? 
            event.getMostAccurateSpeed() : event.getSpeed();
        
        String severity = determineSeverity(speed);
        
        // Only create alerts for significant CMEs (speed >= 500 km/s)
        if (speed != null && speed >= 500) {
            Alert alert = Alert.builder()
                .alertType("CME")
                .severity(severity)
                .cmeSpeed(speed)
                .cmeType(event.getType())
                .latitude(parseCoordinate(event.getLatitude()))
                .longitude(parseCoordinate(event.getLongitude()))
                .description(buildDescription(event, speed))
                .timestamp(event.getStartTime())
                .acknowledged(false)
                .createdAt(Instant.now())
                .build();
            
            // Save to database
            alertRepository.save(alert);
            log.info("Created CME alert: {} - Speed: {} km/s, Type: {}", 
                event.getActivityId(), speed, event.getType());
            
            // Publish to Kafka based on severity
            if ("CRITICAL".equals(severity) || "EXTREME".equals(severity)) {
                kafkaTemplate.send("alerts.critical", alert);
            } else if ("MAJOR".equals(severity) || "MODERATE".equals(severity)) {
                kafkaTemplate.send("alerts.warning", alert);
            }
        }
    }

    private String determineSeverity(Double speed) {
        if (speed == null) {
            return "MINOR";
        }
        
        if (speed >= 2000) {
            return "EXTREME"; // Very fast CME
        } else if (speed >= 1500) {
            return "CRITICAL"; // Fast CME
        } else if (speed >= 1000) {
            return "MAJOR"; // Moderately fast CME
        } else if (speed >= 500) {
            return "MODERATE"; // Moderate CME
        } else {
            return "MINOR"; // Slow CME
        }
    }

    private String buildDescription(CmeEvent event, Double speed) {
        StringBuilder desc = new StringBuilder();
        desc.append(String.format("Coronal Mass Ejection detected with speed of %.0f km/s", speed));
        
        if (event.getType() != null) {
            desc.append(String.format(" (Type: %s)", event.getType()));
        }
        
        if (event.getSourceLocation() != null) {
            desc.append(String.format(". Source: %s", event.getSourceLocation()));
        }
        
        if (speed >= 2000) {
            desc.append(". EXTREME SPEED - High probability of severe geomagnetic storm. " +
                "Satellite operations and power grids may be significantly affected.");
        } else if (speed >= 1500) {
            desc.append(". CRITICAL - Strong geomagnetic storm possible. " +
                "Monitor for potential impacts to satellites and communications.");
        } else if (speed >= 1000) {
            desc.append(". Moderate geomagnetic storm possible. Minor impacts may occur.");
        }
        
        if (event.getNote() != null && !event.getNote().isEmpty()) {
            desc.append(String.format(" Note: %s", event.getNote()));
        }
        
        return desc.toString();
    }
    
    private Double parseCoordinate(String coordinate) {
        if (coordinate == null || coordinate.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(coordinate);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse coordinate: {}", coordinate);
            return null;
        }
    }
}
