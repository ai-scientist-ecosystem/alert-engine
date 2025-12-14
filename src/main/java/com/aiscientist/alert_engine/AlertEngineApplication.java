package com.aiscientist.alert_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableKafka
@EnableAsync
public class AlertEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertEngineApplication.class, args);
	}

}
