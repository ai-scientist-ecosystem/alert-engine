package com.aiscientist.alert_engine.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiscientist.alert_engine.dto.AlertDTO;
import com.aiscientist.alert_engine.dto.KpIndexEvent;
import com.aiscientist.alert_engine.kafka.AlertProducer;
import com.aiscientist.alert_engine.model.Alert;
import com.aiscientist.alert_engine.repository.AlertRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertDetectionService {
    
    private final AlertRepository alertRepository;
    private final AlertProducer alertProducer;
    private final ObjectMapper objectMapper;
    
    @Value("${app.alert.thresholds.kp-index.minor}")
    private Double minorThreshold;
    
    @Value("${app.alert.thresholds.kp-index.moderate}")
    private Double moderateThreshold;
    
    @Value("${app.alert.thresholds.kp-index.strong}")
    private Double strongThreshold;
    
    @Value("${app.alert.thresholds.kp-index.severe}")
    private Double severeThreshold;
    
    @Value("${app.alert.thresholds.kp-index.extreme}")
    private Double extremeThreshold;
    
    @Transactional
    public void analyzeKpIndex(KpIndexEvent event) {
        Double kpValue = event.getKpIndex();
        
        if (kpValue == null || kpValue < minorThreshold) {
            log.debug("Kp-index {} is below alert threshold {}", kpValue, minorThreshold);
            return;
        }
        
        String severity = determineSeverity(kpValue);
        String description = generateDescription(severity, kpValue);
        
        log.info("Geomagnetic storm detected! Kp={}, Severity={}", kpValue, severity);
        
        // Create and save alert
        Alert alert = Alert.builder()
                .alertType("GEOMAGNETIC_STORM")
                .severity(severity)
                .kpValue(kpValue)
                .description(description)
                .timestamp(event.getTimestamp())
                .rawData(serializeToJson(event))
                .build();
        
        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert saved to database: id={}", savedAlert.getId());
        
        // Convert to DTO and publish to Kafka
        AlertDTO alertDTO = convertToDTO(savedAlert);
        
        if (isCriticalSeverity(severity)) {
            alertProducer.sendCriticalAlert(alertDTO);
        } else {
            alertProducer.sendWarningAlert(alertDTO);
        }
    }
    
    private String determineSeverity(Double kpValue) {
        if (kpValue >= extremeThreshold) {
            return "EXTREME";  // Kp >= 8
        } else if (kpValue >= strongThreshold) {
            return "SEVERE";   // Kp >= 6 (G2-G3: Moderate to Strong)
        } else if (kpValue >= moderateThreshold) {
            return "MODERATE"; // Kp >= 5 (G1: Minor)
        } else {
            return "MINOR";    // Kp >= 4
        }
    }
    
    private String generateDescription(String severity, Double kpValue) {
        return switch (severity) {
            case "EXTREME" -> String.format("EXTREME geomagnetic storm detected (Kp=%.2f). " +
                "Widespread power system problems, transformer damage possible. " +
                "Satellite navigation severely degraded. HF radio propagation impossible.", kpValue);
            case "SEVERE" -> String.format("SEVERE geomagnetic storm detected (Kp=%.2f). " +
                "Widespread voltage control problems. Protective systems may trip out key assets. " +
                "Satellite surface charging, navigation degraded for hours.", kpValue);
            case "STRONG" -> String.format("STRONG geomagnetic storm detected (Kp=%.2f). " +
                "Voltage corrections required on power systems. Satellite orientation issues. " +
                "Intermittent satellite navigation and HF radio problems.", kpValue);
            case "MODERATE" -> String.format("MODERATE geomagnetic storm detected (Kp=%.2f). " +
                "High-latitude power systems affected. Satellite drag increased. " +
                "HF radio propagation fades at higher latitudes.", kpValue);
            default -> String.format("MINOR geomagnetic storm detected (Kp=%.2f). " +
                "Weak power grid fluctuations. Minor impact on satellite operations.", kpValue);
        };
    }
    
    private boolean isCriticalSeverity(String severity) {
        return "SEVERE".equals(severity) || "EXTREME".equals(severity);
    }
    
    private String serializeToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            return null;
        }
    }
    
    private AlertDTO convertToDTO(Alert alert) {
        return AlertDTO.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .kpValue(alert.getKpValue())
                .description(alert.getDescription())
                .timestamp(alert.getTimestamp())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
