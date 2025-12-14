# Alert Engine

## 📋 Overview

**Alert Engine** is a microservice that consumes space weather data from Kafka, detects geomagnetic storms based on Kp-index values, and generates real-time alerts. It saves alerts to PostgreSQL and publishes critical alerts to Kafka topics for downstream services.

### Key Features
- ✅ **Real-time Kp-index monitoring** from Kafka topic `raw.spaceweather.kp`
- ✅ **Geomagnetic storm detection** with 5 severity levels (MINOR to EXTREME)
- ✅ **Alert persistence** in PostgreSQL
- ✅ **Kafka event publishing** to `alerts.critical` and `alerts.warning` topics
- ✅ **REST API** for querying alerts
- ✅ **Service discovery** with Eureka
- ✅ **Monitoring** with Prometheus metrics

---

## 🏗️ Architecture

```
┌──────────────────┐
│  Data Collector  │
│  (Port 8082)     │
└────────┬─────────┘
         │ Publishes Kp-index events
         ▼
    ┌────────────┐
    │   Kafka    │
    │ raw.space  │
    │ weather.kp │
    └─────┬──────┘
          │
          ▼
┌─────────────────────┐
│   Alert Engine      │
│   (Port 8083)       │
│                     │
│ 1. Consume events   │
│ 2. Analyze Kp-index │
│ 3. Detect storms    │
│ 4. Save to DB       │
│ 5. Publish alerts   │
└──────┬──────────┬───┘
       │          │
       ▼          ▼
┌──────────┐  ┌────────────┐
│PostgreSQL│  │   Kafka    │
│  alerts  │  │ alerts.*   │
└──────────┘  └────────────┘
```

---

## 🚀 Quick Start

### Prerequisites
- **Java 21+**
- **Maven 3.9+**
- **Docker & Docker Compose**
- **PostgreSQL 15**
- **Apache Kafka**
- **Eureka Server** (running on port 8761)
- **Data Collector** (running on port 8082)

### 1. Build
```bash
cd alert-engine
mvn clean package -DskipTests
```

### 2. Run Locally (IntelliJ IDEA)

**Environment Variables:**
```properties
SPRING_PROFILES_ACTIVE=local
```

**VM Options:**
```
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75.0
```

**Run:** `AlertEngineApplication.java`

### 3. Verify

```bash
# Health check
curl http://localhost:8083/api/v1/alerts/health

# Get all alerts
curl http://localhost:8083/api/v1/alerts

# Get critical alerts
curl http://localhost:8083/api/v1/alerts/critical

# Get alerts by severity
curl http://localhost:8083/api/v1/alerts/severity/SEVERE

# Acknowledge alert
curl -X POST http://localhost:8083/api/v1/alerts/1/acknowledge
```

---

## ⚙️ Configuration

### Kp-Index Severity Thresholds

| Kp Value | Severity | NOAA Scale | Impact |
|----------|----------|------------|--------|
| 4.0 - 4.99 | MINOR | G1 | Weak power grid fluctuations |
| 5.0 - 5.99 | MODERATE | G2 | High-latitude power systems affected |
| 6.0 - 6.99 | STRONG | G3 | Voltage corrections required |
| 7.0 - 7.99 | SEVERE | G4 | Widespread power system problems |
| 8.0+ | EXTREME | G5 | Blackout conditions, transformer damage |

### application.yml

```yaml
app:
  kafka:
    topics:
      raw-spaceweather-kp: raw.spaceweather.kp
      alerts-critical: alerts.critical
      alerts-warning: alerts.warning
  
  alert:
    thresholds:
      kp-index:
        minor: 4.0
        moderate: 5.0
        strong: 6.0
        severe: 7.0
        extreme: 8.0
```

---

## 📡 API Endpoints

### Alert Endpoints

#### GET `/api/v1/alerts`
Get recent alerts (default: last 24 hours)

**Query Parameters:**
- `hours` (optional, default: 24) - Time range in hours

**Response:**
```json
[
  {
    "id": 1,
    "alertType": "GEOMAGNETIC_STORM",
    "severity": "SEVERE",
    "kpValue": 7.33,
    "description": "SEVERE geomagnetic storm detected...",
    "timestamp": "2025-12-08T10:00:00Z",
    "createdAt": "2025-12-08T10:01:00Z"
  }
]
```

#### GET `/api/v1/alerts/severity/{severity}`
Get alerts by severity level

**Path Parameters:**
- `severity` - MINOR, MODERATE, STRONG, SEVERE, EXTREME

#### GET `/api/v1/alerts/critical`
Get critical unacknowledged alerts (SEVERE & EXTREME)

#### POST `/api/v1/alerts/{id}/acknowledge`
Acknowledge an alert

---

## 🔧 Database Schema

### `alerts` Table

```sql
CREATE TABLE alerts (
    id BIGSERIAL PRIMARY KEY,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    kp_value DOUBLE PRECISION,
    description TEXT,
    timestamp TIMESTAMP NOT NULL,
    raw_data TEXT,
    created_at TIMESTAMP NOT NULL,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_at TIMESTAMP
);

CREATE INDEX idx_alert_severity ON alerts(severity);
CREATE INDEX idx_alert_timestamp ON alerts(timestamp);
CREATE INDEX idx_alert_type ON alerts(alert_type);
```

---

## 🐳 Docker Deployment

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/alert-engine-1.0.0.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build & Run

```bash
docker build -t alert-engine:1.0.0 .
docker run -d -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/ai_scientist \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  --name alert-engine \
  alert-engine:1.0.0
```

---

## 📊 Monitoring

### Prometheus Metrics

```
http://localhost:8083/actuator/prometheus
```

**Key Metrics:**
- `kafka_consumer_records_consumed_total` - Total Kafka messages consumed
- `alerts_generated_total` - Total alerts generated
- `alerts_critical_total` - Critical alerts count
- `jvm_memory_used_bytes` - JVM memory usage

---

## 🔐 Security

✅ **Input Validation** - Jakarta Bean Validation  
✅ **SQL Injection Prevention** - JPA with prepared statements  
✅ **Error Handling** - No stack traces exposed  
✅ **Kafka Security** - Idempotent producers  

---

## 🧪 Testing

```bash
# Run unit tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

---

## 📦 Dependencies

| Dependency | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 3.5.0 | Framework |
| Spring Cloud | 2025.0.0 | Eureka client |
| Spring Kafka | 3.3.0 | Kafka integration |
| PostgreSQL | 42.7.7 | Database driver |
| Resilience4j | 2.2.0 | Circuit breaker |
| Lombok | 1.18.36 | Boilerplate reduction |

---

## 🤝 Contributing

See [Backend Agent Checklist](../../.github/ai-agents/backend-agent.md) for code quality guidelines.

---

## 📄 License

MIT License - See [LICENSE](../meta/LICENSE) for details.

---

## 🔗 Related Services

- **data-collector** - Collects space weather data and publishes to Kafka
- **alert-publisher** - Consumes alerts and sends notifications
- **api-gateway** - Unified API for frontend
