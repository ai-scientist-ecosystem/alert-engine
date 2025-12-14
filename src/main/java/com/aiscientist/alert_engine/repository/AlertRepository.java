package com.aiscientist.alert_engine.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aiscientist.alert_engine.model.Alert;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {
    
    List<Alert> findByTimestampAfter(Instant timestamp);
    
    List<Alert> findBySeverity(String severity);
    
    List<Alert> findByAlertType(String alertType);
    
    List<Alert> findByAcknowledged(Boolean acknowledged);
    
    @Query("SELECT a FROM Alert a WHERE a.timestamp >= :startTime AND a.timestamp <= :endTime ORDER BY a.timestamp DESC")
    List<Alert> findAlertsBetween(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    
    @Query("SELECT a FROM Alert a WHERE a.severity IN ('SEVERE', 'EXTREME') AND a.acknowledged = false ORDER BY a.timestamp DESC")
    List<Alert> findCriticalUnacknowledged();
    
    // Earthquake-specific queries
    List<Alert> findByAlertTypeAndMagnitudeGreaterThanEqual(String alertType, Double magnitude);
    
    @Query("SELECT a FROM Alert a WHERE a.alertType = 'EARTHQUAKE' AND a.region = :region ORDER BY a.timestamp DESC")
    List<Alert> findEarthquakeAlertsByRegion(@Param("region") String region);
    
    @Query("SELECT a FROM Alert a WHERE a.alertType = 'EARTHQUAKE' AND a.earthquakeId = :earthquakeId")
    Alert findByEarthquakeId(@Param("earthquakeId") String earthquakeId);
    
    // Tsunami-specific queries
    @Query("SELECT a FROM Alert a WHERE a.alertType = 'TSUNAMI' ORDER BY a.timestamp DESC")
    List<Alert> findTsunamiAlerts();
    
    @Query("SELECT a FROM Alert a WHERE a.alertType = 'TSUNAMI' AND a.tsunamiRiskScore >= :minRiskScore ORDER BY a.timestamp DESC")
    List<Alert> findTsunamiAlertsByRiskScore(@Param("minRiskScore") Integer minRiskScore);
    
    // Flood-specific queries
    @Query("SELECT a FROM Alert a WHERE a.alertType = 'FLOOD' ORDER BY a.timestamp DESC")
    List<Alert> findFloodAlerts();
    
    @Query("SELECT a FROM Alert a WHERE a.alertType = 'FLOOD' AND a.stationId = :stationId ORDER BY a.timestamp DESC")
    List<Alert> findFloodAlertsByStation(@Param("stationId") String stationId);
    
    // CME-specific queries
    @Query("SELECT a FROM Alert a WHERE a.alertType = 'CME' ORDER BY a.timestamp DESC")
    List<Alert> findCmeAlerts();
    
    @Query("SELECT a FROM Alert a WHERE a.alertType = 'CME' AND a.cmeSpeed >= :minSpeed ORDER BY a.timestamp DESC")
    List<Alert> findCmeAlertsBySpeed(@Param("minSpeed") Double minSpeed);
    
    // Geographic queries
    @Query("SELECT a FROM Alert a WHERE a.latitude BETWEEN :minLat AND :maxLat " +
           "AND a.longitude BETWEEN :minLon AND :maxLon ORDER BY a.timestamp DESC")
    List<Alert> findAlertsByGeographicArea(
        @Param("minLat") Double minLat, 
        @Param("maxLat") Double maxLat,
        @Param("minLon") Double minLon, 
        @Param("maxLon") Double maxLon
    );
}
