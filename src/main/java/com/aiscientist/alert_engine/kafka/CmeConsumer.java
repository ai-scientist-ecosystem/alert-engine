package com.aiscientist.alert_engine.kafka;

import com.aiscientist.alert_engine.dto.CmeEvent;
import com.aiscientist.alert_engine.service.CmeAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CmeConsumer {

    private final CmeAlertService cmeAlertService;

    @KafkaListener(
        topics = "raw.spaceweather.cme", 
        groupId = "${spring.kafka.consumer.group-id}",
        properties = {
            "spring.json.value.default.type=com.aiscientist.alert_engine.dto.CmeEvent"
        }
    )
    public void consumeCmeEvent(CmeEvent event) {
        try {
            log.info("Received CME event: {} - Speed: {} km/s, Type: {}", 
                event.getActivityId(), event.getSpeed(), event.getType());
            
            cmeAlertService.processCmeEvent(event);
            
        } catch (Exception e) {
            log.error("Error processing CME event: {}", event.getActivityId(), e);
        }
    }
}
