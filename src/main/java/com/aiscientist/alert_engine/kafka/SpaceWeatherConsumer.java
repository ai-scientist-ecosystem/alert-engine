package com.aiscientist.alert_engine.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.aiscientist.alert_engine.dto.KpIndexEvent;
import com.aiscientist.alert_engine.service.AlertDetectionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpaceWeatherConsumer {
    
    private final AlertDetectionService alertDetectionService;
    
    @KafkaListener(
        topics = "${app.kafka.topics.raw-spaceweather-kp}",
        groupId = "${spring.kafka.consumer.group-id}",
        properties = {
            "spring.json.value.default.type=com.aiscientist.alert_engine.dto.KpIndexEvent"
        }
    )
    public void consumeKpIndexEvent(KpIndexEvent event) {
        try {
            log.info("Received Kp-index event: kp={}, timestamp={}", event.getKpIndex(), event.getTimestamp());
            
            // Process the event and detect alerts
            alertDetectionService.analyzeKpIndex(event);
            
        } catch (Exception e) {
            log.error("Error processing Kp-index event: {}", event, e);
            // In production, you might want to send to Dead Letter Queue
        }
    }
}
