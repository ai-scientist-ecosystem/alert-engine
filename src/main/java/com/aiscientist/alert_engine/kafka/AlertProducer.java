package com.aiscientist.alert_engine.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.aiscientist.alert_engine.dto.AlertDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${app.kafka.topics.alerts-critical}")
    private String criticalAlertsTopic;
    
    @Value("${app.kafka.topics.alerts-warning}")
    private String warningAlertsTopic;
    
    public void sendCriticalAlert(AlertDTO alert) {
        String key = alert.getAlertType() + "-" + alert.getSeverity();
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(criticalAlertsTopic, key, alert);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Critical alert published: {} [offset={}]", 
                    alert.getAlertType(), 
                    result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish critical alert: {}", alert, ex);
            }
        });
    }
    
    public void sendWarningAlert(AlertDTO alert) {
        String key = alert.getAlertType() + "-" + alert.getSeverity();
        
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(warningAlertsTopic, key, alert);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Warning alert published: {} [offset={}]", 
                    alert.getAlertType(), 
                    result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish warning alert: {}", alert, ex);
            }
        });
    }
}
