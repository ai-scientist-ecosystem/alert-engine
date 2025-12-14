package com.aiscientist.alert_engine.kafka;

import com.aiscientist.alert_engine.dto.FloodAlertEvent;
import com.aiscientist.alert_engine.service.FloodAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FloodConsumer {

    private final FloodAlertService floodAlertService;

    @KafkaListener(
        topics = "raw.flood.alert", 
        groupId = "${spring.kafka.consumer.group-id}",
        properties = {
            "spring.json.value.default.type=com.aiscientist.alert_engine.dto.FloodAlertEvent"
        }
    )
    public void consumeFloodAlert(FloodAlertEvent event) {
        try {
            log.info("Received flood alert: {} - {} - Severity: {}, Water Level: {} ft", 
                event.getStationId(), event.getStationName(), 
                event.getFloodSeverity(), event.getWaterLevelFeet());
            
            floodAlertService.processFloodAlert(event);
            
        } catch (Exception e) {
            log.error("Error processing flood alert: {}", event.getStationId(), e);
        }
    }
}
