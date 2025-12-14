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
public class EarthquakeAlertService {

    private final AlertRepository alertRepository;
    private final KafkaTemplate<String, Alert> kafkaTemplate;

    public void processEarthquakeEvent(EarthquakeEvent event) {
        // Determine severity based on magnitude
        String severity = determineSeverity(event.getMagnitude());
        
        // Only create alerts for significant earthquakes (magnitude >= 5.0)
        if (event.getMagnitude() >= 5.0) {
            Alert alert = Alert.builder()
                .alertType("EARTHQUAKE")
                .severity(severity)
                .earthquakeId(event.getEarthquakeId())
                .magnitude(event.getMagnitude())
                .depthKm(event.getDepthKm())
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
            log.info("Created earthquake alert: {} - {} magnitude at {}", 
                event.getEarthquakeId(), event.getMagnitude(), event.getLocation());
            
            // Publish to Kafka based on severity
            if ("CRITICAL".equals(severity) || "MAJOR".equals(severity)) {
                kafkaTemplate.send("alerts.critical", alert);
            } else if ("MODERATE".equals(severity)) {
                kafkaTemplate.send("alerts.warning", alert);
            }
        }
    }

    private String determineSeverity(Double magnitude) {
        if (magnitude >= 8.0) {
            return "EXTREME"; // Great earthquake
        } else if (magnitude >= 7.0) {
            return "CRITICAL"; // Major earthquake
        } else if (magnitude >= 6.0) {
            return "MAJOR"; // Strong earthquake
        } else if (magnitude >= 5.0) {
            return "MODERATE"; // Moderate earthquake
        } else {
            return "MINOR"; // Light earthquake
        }
    }

    private String buildDescription(EarthquakeEvent event) {
        StringBuilder desc = new StringBuilder();
        desc.append(String.format("Magnitude %.1f earthquake detected at %s", 
            event.getMagnitude(), event.getLocation()));
        
        if (event.getDepthKm() != null) {
            desc.append(String.format(", depth: %.1f km", event.getDepthKm()));
        }
        
        if (event.getDangerous() != null && event.getDangerous()) {
            desc.append(". WARNING: This is classified as a dangerous earthquake.");
        }
        
        if (event.getCatastrophic() != null && event.getCatastrophic()) {
            desc.append(" CATASTROPHIC EVENT - Expect severe damage.");
        }
        
        if (event.getTsunamiWarning() != null && event.getTsunamiWarning()) {
            desc.append(" TSUNAMI WARNING ISSUED.");
        }
        
        return desc.toString();
    }
}
