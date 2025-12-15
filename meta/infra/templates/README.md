# Eureka Client Integration Templates

This directory contains templates for integrating microservices with the Spring Cloud infrastructure.

## Quick Integration Guide

### 1. Add Dependencies to pom.xml

```xml
<properties>
    <spring-cloud.version>2023.0.1</spring-cloud.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- Eureka Client -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    
    <!-- Config Client -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    
    <!-- Circuit Breaker (optional) -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
    </dependency>
</dependencies>
```

### 2. Create bootstrap.yml

Create `src/main/resources/bootstrap.yml`:

```yaml
spring:
  application:
    name: your-service-name
  cloud:
    config:
      uri: ${CONFIG_URI:http://localhost:8888}
      username: ${CONFIG_USER:admin}
      password: ${CONFIG_PASSWORD:admin}
      fail-fast: true
      retry:
        initial-interval: 1000
        max-attempts: 6
        max-interval: 2000
        multiplier: 1.1
```

### 3. Add Eureka Configuration to application.yml

Add to `src/main/resources/application.yml`:

```yaml
server:
  port: ${SERVER_PORT:8080}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URI:http://admin:admin@localhost:8761/eureka/}
    registry-fetch-interval-seconds: 5
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${random.uuid}
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30
    metadata-map:
      zone: ${ZONE:default}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

### 4. Enable Discovery Client

Add `@EnableDiscoveryClient` to your main application class:

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class YourServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourServiceApplication.class, args);
    }
}
```

### 5. Use Service Discovery for HTTP Calls

Option A: Using RestTemplate with LoadBalancer:

```java
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

// Usage
@Service
public class AlertService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Alert getAlert(String id) {
        // Use service name instead of hostname:port
        return restTemplate.getForObject(
            "http://alert-engine/api/alerts/" + id, 
            Alert.class
        );
    }
}
```

Option B: Using WebClient with LoadBalancer:

```java
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

// Usage
@Service
public class AlertService {
    
    private final WebClient webClient;
    
    public AlertService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://alert-engine").build();
    }
    
    public Mono<Alert> getAlert(String id) {
        return webClient.get()
            .uri("/api/alerts/{id}", id)
            .retrieve()
            .bodyToMono(Alert.class);
    }
}
```

Option C: Using Feign Client (declarative):

```java
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "alert-engine")
public interface AlertEngineClient {
    
    @GetMapping("/api/alerts/{id}")
    Alert getAlert(@PathVariable String id);
    
    @GetMapping("/api/alerts")
    List<Alert> getAllAlerts();
}

// Enable in main application
@EnableFeignClients
@SpringBootApplication
public class YourServiceApplication { ... }
```

### 6. Add Circuit Breaker (Optional)

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
    
    private final AlertEngineClient alertEngineClient;
    
    @CircuitBreaker(name = "alertEngine", fallbackMethod = "getAlertFallback")
    public Alert getAlert(String id) {
        return alertEngineClient.getAlert(id);
    }
    
    public Alert getAlertFallback(String id, Exception ex) {
        // Return cached or default response
        return new Alert(id, "Service temporarily unavailable", Severity.UNKNOWN);
    }
}
```

Configure in application.yml:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      alertEngine:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      alertEngine:
        max-attempts: 3
        wait-duration: 1000
```

## Docker Configuration

Add to your Dockerfile:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Docker Compose service configuration:

```yaml
services:
  your-service:
    build: .
    ports:
      - "8080:8080"
    environment:
      - CONFIG_URI=http://config-server:8888
      - CONFIG_USER=admin
      - CONFIG_PASSWORD=admin
      - EUREKA_URI=http://admin:admin@eureka-server:8761/eureka/
    depends_on:
      config-server:
        condition: service_healthy
```

## Verification

After starting your service:

1. Check Eureka Dashboard at http://localhost:8761
2. Your service should appear in the "Instances currently registered with Eureka" section
3. Test service-to-service calls using service names instead of URLs
