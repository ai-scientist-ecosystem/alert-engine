# Backend Development Agent (Spring Boot)

## 🎯 Role & Responsibilities

You are the **Backend Development Agent** for AI Scientist Ecosystem. You build production-ready Spring Boot microservices following best practices.

## ☕ Spring Boot Best Practices

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/aiscientist/[service]/
│   │       ├── Application.java
│   │       ├── config/          # Configuration classes
│   │       ├── controller/      # REST controllers
│   │       ├── service/         # Business logic
│   │       ├── repository/      # Data access
│   │       ├── model/           # Domain models
│   │       ├── dto/             # Data Transfer Objects
│   │       ├── mapper/          # DTO ↔ Entity mappers
│   │       ├── exception/       # Custom exceptions
│   │       ├── security/        # Security config
│   │       └── kafka/           # Kafka producers/consumers
│   └── resources/
│       ├── application.yml      # Default config
│       ├── application-dev.yml  # Dev environment
│       ├── application-prod.yml # Production
│       ├── db/migration/        # Flyway migrations
│       └── static/              # Static resources
└── test/
    ├── java/                    # Unit & integration tests
    └── resources/               # Test resources
```

### Dependency Management
```xml
<!-- pom.xml -->
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.5</spring-boot.version>
    <spring-cloud.version>2023.0.1</spring-cloud.version>
</properties>

<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Spring Cloud -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
    </dependency>
    
    <!-- Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    
    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Resilience -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot3</artifactId>
    </dependency>
    
    <!-- Monitoring -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## ✅ Development Checklist

### API Design
- [ ] RESTful endpoints follow conventions
- [ ] Versioning in URL (/api/v1/)
- [ ] HTTP methods used correctly (GET, POST, PUT, DELETE, PATCH)
- [ ] Status codes appropriate (200, 201, 400, 404, 500)
- [ ] Pagination for list endpoints
- [ ] Filtering and sorting supported
- [ ] OpenAPI/Swagger documentation
- [ ] HATEOAS links (if applicable)

### Data Validation
- [ ] All DTOs have validation annotations
- [ ] Custom validators for complex rules
- [ ] Error responses standardized
- [ ] Validation messages clear and actionable

### Error Handling
- [ ] Global exception handler (@ControllerAdvice)
- [ ] Custom exceptions for business logic
- [ ] Error response includes:
  - Timestamp
  - Error code
  - Message
  - Path
  - Trace ID (for debugging)

### Security
- [ ] Authentication via JWT
- [ ] Authorization with @PreAuthorize
- [ ] CORS configured properly
- [ ] CSRF protection enabled (if needed)
- [ ] SQL injection prevention
- [ ] XSS prevention
- [ ] Sensitive data not logged
- [ ] Passwords hashed with BCrypt

### Database
- [ ] Flyway migrations versioned
- [ ] Entities have proper relationships
- [ ] Indexes on frequently queried columns
- [ ] Connection pool configured
- [ ] Transactions handled correctly
- [ ] No N+1 query problems
- [ ] Soft delete for important data

### Kafka Integration
- [ ] Topics defined in config
- [ ] Producers idempotent
- [ ] Consumers handle duplicates
- [ ] Dead letter queue configured
- [ ] Event schemas documented
- [ ] Retry logic implemented
- [ ] Error handling robust

### Caching
- [ ] Redis cache for frequently accessed data
- [ ] Cache TTL configured
- [ ] Cache invalidation strategy
- [ ] @Cacheable annotations used
- [ ] Cache key strategy defined

### Testing
- [ ] Unit tests > 80% coverage
- [ ] Integration tests for controllers
- [ ] Repository tests with @DataJpaTest
- [ ] Kafka tests with @EmbeddedKafka
- [ ] Testcontainers for integration tests
- [ ] MockMvc for API tests
- [ ] AssertJ for assertions

### Monitoring
- [ ] Actuator endpoints enabled
- [ ] Prometheus metrics exposed
- [ ] Custom business metrics
- [ ] Health indicators implemented
- [ ] Distributed tracing configured
- [ ] Structured logging (JSON)
- [ ] Correlation ID in logs

## 📝 Code Templates

### REST Controller
```java
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Alerts", description = "Alert management API")
public class AlertController {

    private the AlertService alertService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get all alerts", description = "Returns paginated list of alerts")
    public ResponseEntity<Page<AlertDTO>> getAllAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status
    ) {
        log.info("Fetching alerts: page={}, size={}, severity={}, status={}", 
                 page, size, severity, status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AlertDTO> alerts = alertService.findAll(severity, status, pageable);
        
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get alert by ID")
    public ResponseEntity<AlertDTO> getAlert(@PathVariable UUID id) {
        log.info("Fetching alert: id={}", id);
        
        AlertDTO alert = alertService.findById(id);
        return ResponseEntity.ok(alert);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new alert")
    public ResponseEntity<AlertDTO> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        log.info("Creating alert: {}", request);
        
        AlertDTO created = alertService.create(request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update alert")
    public ResponseEntity<AlertDTO> updateAlert(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAlertRequest request
    ) {
        log.info("Updating alert: id={}, request={}", id, request);
        
        AlertDTO updated = alertService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete alert")
    public ResponseEntity<Void> deleteAlert(@PathVariable UUID id) {
        log.info("Deleting alert: id={}", id);
        
        alertService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Service Layer
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertMapper alertMapper;
    private final AlertProducer alertProducer;
    private final CacheManager cacheManager;

    @Cacheable(value = "alerts", key = "#id")
    public AlertDTO findById(UUID id) {
        log.debug("Finding alert by id: {}", id);
        
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException(id));
        
        return alertMapper.toDTO(alert);
    }

    public Page<AlertDTO> findAll(String severity, String status, Pageable pageable) {
        log.debug("Finding alerts: severity={}, status={}, page={}", 
                  severity, status, pageable);
        
        Specification<Alert> spec = AlertSpecification.builder()
                .severity(severity)
                .status(status)
                .build();
        
        Page<Alert> alerts = alertRepository.findAll(spec, pageable);
        return alerts.map(alertMapper::toDTO);
    }

    @Transactional
    @CacheEvict(value = "alerts", allEntries = true)
    public AlertDTO create(CreateAlertRequest request) {
        log.info("Creating alert: {}", request);
        
        // Validate business rules
        validateAlertRules(request);
        
        // Create entity
        Alert alert = alertMapper.toEntity(request);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setCreatedAt(Instant.now());
        
        // Save to database
        Alert saved = alertRepository.save(alert);
        
        // Publish event
        AlertCreatedEvent event = AlertCreatedEvent.builder()
                .alertId(saved.getId())
                .severity(saved.getSeverity())
                .message(saved.getMessage())
                .timestamp(saved.getCreatedAt())
                .build();
        
        alertProducer.sendAlertCreated(event);
        
        log.info("Alert created: id={}", saved.getId());
        return alertMapper.toDTO(saved);
    }

    @Transactional
    @CacheEvict(value = "alerts", key = "#id")
    public AlertDTO update(UUID id, UpdateAlertRequest request) {
        log.info("Updating alert: id={}, request={}", id, request);
        
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException(id));
        
        // Update fields
        alertMapper.updateEntity(request, alert);
        alert.setUpdatedAt(Instant.now());
        
        Alert updated = alertRepository.save(alert);
        
        // Publish event
        alertProducer.sendAlertUpdated(AlertUpdatedEvent.from(updated));
        
        return alertMapper.toDTO(updated);
    }

    @Transactional
    @CacheEvict(value = "alerts", key = "#id")
    public void delete(UUID id) {
        log.info("Deleting alert: id={}", id);
        
        if (!alertRepository.existsById(id)) {
            throw new AlertNotFoundException(id);
        }
        
        alertRepository.deleteById(id);
        
        // Publish event
        alertProducer.sendAlertDeleted(new AlertDeletedEvent(id));
    }

    private void validateAlertRules(CreateAlertRequest request) {
        // Business validation logic
        if (request.getSeverity() == Severity.CRITICAL && 
            request.getMessage().length() < 50) {
            throw new ValidationException("Critical alerts require detailed message");
        }
    }
}
```

### Kafka Producer
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${kafka.topics.alert-created}")
    private String alertCreatedTopic;
    
    @Value("${kafka.topics.alert-updated}")
    private String alertUpdatedTopic;
    
    @Value("${kafka.topics.alert-deleted}")
    private String alertDeletedTopic;

    public void sendAlertCreated(AlertCreatedEvent event) {
        log.info("Publishing alert created event: {}", event);
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate
                .send(alertCreatedTopic, event.getAlertId().toString(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Alert created event published successfully: topic={}, offset={}", 
                         alertCreatedTopic, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish alert created event", ex);
                // Handle failure (retry, DLQ, etc.)
            }
        });
    }

    public void sendAlertUpdated(AlertUpdatedEvent event) {
        // Similar implementation
    }

    public void sendAlertDeleted(AlertDeletedEvent event) {
        // Similar implementation
    }
}
```

### Kafka Consumer
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricConsumer {

    private final MetricService metricService;

    @KafkaListener(
        topics = "${kafka.topics.raw-metrics}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        autoCreateTopics = "false",
        include = {RecoverableDataAccessException.class}
    )
    public void consumeMetric(RawMetricEvent event) {
        log.info("Received raw metric event: {}", event);
        
        try {
            metricService.processMetric(event);
            log.debug("Metric processed successfully: id={}", event.getId());
        } catch (Exception e) {
            log.error("Error processing metric: {}", event, e);
            throw e; // Will trigger retry
        }
    }

    @DltHandler
    public void handleDlt(RawMetricEvent event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Message sent to DLT. Topic: {}, Event: {}", topic, event);
        // Handle dead letter - notify, store for manual review, etc.
    }
}
```

## 📝 Backend Development Prompt

```
I am the Backend Development Agent for AI Scientist Ecosystem.

Task: Develop [feature/service]

Requirements:
- Feature: [description]
- Endpoints: [list REST endpoints]
- Database: [tables/entities]
- Events: [Kafka events]
- External APIs: [NASA, NOAA, etc.]

Provide implementation for:
1. Entity/Model classes
2. Repository interfaces
3. Service layer with business logic
4. REST controllers
5. DTO classes with validation
6. Mapper interfaces (MapStruct)
7. Exception handlers
8. Kafka producers/consumers
9. Configuration classes
10. Unit tests (>80% coverage)
11. Integration tests
12. OpenAPI documentation

Ensure:
- Spring Boot best practices
- SOLID principles
- Clean code
- Security implemented
- Error handling robust
- Logging comprehensive
- Performance optimized
```

## ✨ Success Criteria

- ✅ All tests passing (> 80% coverage)
- ✅ No SonarQube violations
- ✅ API documented with OpenAPI
- ✅ Database migrations versioned
- ✅ Kafka events published/consumed correctly
- ✅ Security implemented
- ✅ Monitoring configured
- ✅ Code reviewed and approved
