package com.aiscientist.alert_engine.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aiscientist.alert_engine.dto.AlertDTO;
import com.aiscientist.alert_engine.model.Alert;
import com.aiscientist.alert_engine.repository.AlertRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {
    
    private final AlertRepository alertRepository;
    
    @GetMapping
    public ResponseEntity<List<AlertDTO>> getAllAlerts(
            @RequestParam(defaultValue = "24") int hours) {
        
        Instant startTime = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<Alert> alerts = alertRepository.findByTimestampAfter(startTime);
        
        List<AlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} alerts from last {} hours", alertDTOs.size(), hours);
        return ResponseEntity.ok(alertDTOs);
    }
    
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<AlertDTO>> getAlertsBySeverity(@PathVariable String severity) {
        List<Alert> alerts = alertRepository.findBySeverity(severity.toUpperCase());
        
        List<AlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(alertDTOs);
    }
    
    @GetMapping("/critical")
    public ResponseEntity<List<AlertDTO>> getCriticalAlerts() {
        List<Alert> alerts = alertRepository.findCriticalUnacknowledged();
        
        List<AlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} critical unacknowledged alerts", alertDTOs.size());
        return ResponseEntity.ok(alertDTOs);
    }
    
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<AlertDTO> acknowledgeAlert(@PathVariable UUID id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + id));
        
        alert.setAcknowledged(true);
        alert.setAcknowledgedAt(Instant.now());
        
        Alert updated = alertRepository.save(alert);
        log.info("Alert {} acknowledged", id);
        
        return ResponseEntity.ok(convertToDTO(updated));
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Alert Engine is running");
    }
    
    // Earthquake endpoints
    @GetMapping("/earthquakes")
    public ResponseEntity<List<AlertDTO>> getEarthquakeAlerts(
            @RequestParam(required = false) Double minMagnitude,
            @RequestParam(required = false) String region) {
        
        List<Alert> alerts;
        if (minMagnitude != null) {
            alerts = alertRepository.findByAlertTypeAndMagnitudeGreaterThanEqual("EARTHQUAKE", minMagnitude);
        } else if (region != null) {
            alerts = alertRepository.findEarthquakeAlertsByRegion(region);
        } else {
            alerts = alertRepository.findByAlertType("EARTHQUAKE");
        }
        
        List<AlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} earthquake alerts", alertDTOs.size());
        return ResponseEntity.ok(alertDTOs);
    }
    
    @GetMapping("/earthquakes/{earthquakeId}")
    public ResponseEntity<AlertDTO> getEarthquakeAlert(@PathVariable String earthquakeId) {
        Alert alert = alertRepository.findByEarthquakeId(earthquakeId);
        
        if (alert == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(convertToDTO(alert));
    }
    
    // Tsunami endpoints
    @GetMapping("/tsunamis")
    public ResponseEntity<List<AlertDTO>> getTsunamiAlerts(
            @RequestParam(required = false, defaultValue = "0") Integer minRiskScore) {
        
        List<Alert> alerts;
        if (minRiskScore > 0) {
            alerts = alertRepository.findTsunamiAlertsByRiskScore(minRiskScore);
        } else {
            alerts = alertRepository.findTsunamiAlerts();
        }
        
        List<AlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} tsunami alerts", alertDTOs.size());
        return ResponseEntity.ok(alertDTOs);
    }
    
    // Flood endpoints
    @GetMapping("/floods")
    public ResponseEntity<List<AlertDTO>> getFloodAlerts(
            @RequestParam(required = false) String stationId) {
        
        List<Alert> alerts;
        if (stationId != null) {
            alerts = alertRepository.findFloodAlertsByStation(stationId);
        } else {
            alerts = alertRepository.findFloodAlerts();
        }
        
        List<AlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} flood alerts", alertDTOs.size());
        return ResponseEntity.ok(alertDTOs);
    }
    
    // CME endpoints
    @GetMapping("/cme")
    public ResponseEntity<List<AlertDTO>> getCmeAlerts(
            @RequestParam(required = false) Double minSpeed) {
        
        List<Alert> alerts;
        if (minSpeed != null) {
            alerts = alertRepository.findCmeAlertsBySpeed(minSpeed);
        } else {
            alerts = alertRepository.findCmeAlerts();
        }
        
        List<AlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} CME alerts", alertDTOs.size());
        return ResponseEntity.ok(alertDTOs);
    }
    
    // Geographic search endpoint
    @GetMapping("/location")
    public ResponseEntity<List<AlertDTO>> getAlertsByLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusDegrees) {
        
        Double minLat = latitude - radiusDegrees;
        Double maxLat = latitude + radiusDegrees;
        Double minLon = longitude - radiusDegrees;
        Double maxLon = longitude + radiusDegrees;
        
        List<Alert> alerts = alertRepository.findAlertsByGeographicArea(minLat, maxLat, minLon, maxLon);
        
        List<AlertDTO> alertDTOs = alerts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        log.info("Retrieved {} alerts near location ({}, {})", alertDTOs.size(), latitude, longitude);
        return ResponseEntity.ok(alertDTOs);
    }
    
    @GetMapping("/types")
    public ResponseEntity<List<String>> getAlertTypes() {
        return ResponseEntity.ok(List.of("EARTHQUAKE", "TSUNAMI", "FLOOD", "CME", "SPACE_WEATHER"));
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
