package com.aiscientist.alert_engine.kafka;

import com.aiscientist.alert_engine.dto.EarthquakeEvent;
import com.aiscientist.alert_engine.service.TsunamiAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TsunamiConsumer {

    private final TsunamiAlertService tsunamiAlertService;

    @KafkaListener(
        topics = "raw.tsunami.warning", 
        groupId = "${spring.kafka.consumer.group-id}",
        properties = {
            "spring.json.value.default.type=com.aiscientist.alert_engine.dto.EarthquakeEvent"
        }
    )
    public void consumeTsunamiWarning(EarthquakeEvent event) {
        try {
            log.warn("Received tsunami warning: {} - Risk Score: {}, Location: {}", 
                event.getEarthquakeId(), event.getTsunamiRiskScore(), event.getLocation());
            
            tsunamiAlertService.processTsunamiWarning(event);
            
        } catch (Exception e) {
            log.error("Error processing tsunami warning: {}", event.getEarthquakeId(), e);
        }
    }
}
