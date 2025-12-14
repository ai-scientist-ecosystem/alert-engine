package com.aiscientist.alert_engine.kafka;

import com.aiscientist.alert_engine.dto.EarthquakeEvent;
import com.aiscientist.alert_engine.service.EarthquakeAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EarthquakeConsumer {

    private final EarthquakeAlertService earthquakeAlertService;

    @KafkaListener(
        topics = {"raw.earthquake.data", "raw.earthquake.alert"}, 
        groupId = "${spring.kafka.consumer.group-id}",
        properties = {
            "spring.json.value.default.type=com.aiscientist.alert_engine.dto.EarthquakeEvent"
        }
    )
    public void consumeEarthquakeEvent(EarthquakeEvent event) {
        try {
            log.info("Received earthquake event: {} - Magnitude: {}, Location: {}", 
                event.getEarthquakeId(), event.getMagnitude(), event.getLocation());
            
            earthquakeAlertService.processEarthquakeEvent(event);
            
        } catch (Exception e) {
            log.error("Error processing earthquake event: {}", event.getEarthquakeId(), e);
        }
    }
}
