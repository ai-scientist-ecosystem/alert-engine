# Alert Engine Test Guide

## Prerequisites Check
✅ PostgreSQL running on port 5433
✅ Kafka running on port 9092
✅ Eureka Server running on port 8761
✅ Redis running on port 6379

## Test Steps

### 1. Start Data Collector (Terminal 1)
```bash
cd D:\Thai\root\AI-Scientist-Ecosystem\data-collector
# Run in IntelliJ or terminal:
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Expected Output:**
- Application starts on port 8082
- Registers with Eureka as "DATA-COLLECTOR"
- Scheduler triggers Kp-index collection every 10 minutes
- Publishes events to Kafka topic `raw.spaceweather.kp`

### 2. Start Alert Engine (Terminal 2)
```bash
cd D:\Thai\root\AI-Scientist-Ecosystem\alert-engine
# Run in IntelliJ or terminal:
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Expected Output:**
- Application starts on port 8083
- Registers with Eureka as "ALERT-ENGINE"
- Kafka consumer subscribes to `raw.spaceweather.kp`
- Ready to process Kp-index events

### 3. Verify Alert Engine is Running
```powershell
# Health check
curl http://localhost:8083/api/v1/alerts/health

# Expected: "Alert Engine is running"
```

### 4. Trigger Data Collection Manually
```powershell
# Trigger Kp-index collection
curl -X POST http://localhost:8082/api/v1/collector/collect/kp-index

# Expected: "Kp index data collection triggered"
```

### 5. Check Kafka Messages (Kafka UI)
Open: http://localhost:8080

Navigate to:
- Topics → `raw.spaceweather.kp`
- Should see messages with Kp-index data

### 6. Verify Alerts Generated
```powershell
# Get all alerts
curl http://localhost:8083/api/v1/alerts

# Get critical alerts (Kp >= 7.0)
curl http://localhost:8083/api/v1/alerts/critical

# Get alerts by severity
curl http://localhost:8083/api/v1/alerts/severity/SEVERE
```

### 7. Check Database
```sql
-- Connect to PostgreSQL
psql -h localhost -p 5433 -U ai_user -d ai_scientist

-- Check alerts table
SELECT id, alert_type, severity, kp_value, timestamp, created_at 
FROM alerts 
ORDER BY created_at DESC 
LIMIT 10;

-- Count by severity
SELECT severity, COUNT(*) 
FROM alerts 
GROUP BY severity 
ORDER BY 
  CASE severity 
    WHEN 'EXTREME' THEN 1 
    WHEN 'SEVERE' THEN 2 
    WHEN 'STRONG' THEN 3 
    WHEN 'MODERATE' THEN 4 
    WHEN 'MINOR' THEN 5 
  END;
```

### 8. Verify Eureka Registration
Open: http://localhost:8761

Should see:
- DATA-COLLECTOR (1 instance on port 8082)
- ALERT-ENGINE (1 instance on port 8083)

### 9. Check Prometheus Metrics
```powershell
# Data Collector metrics
curl http://localhost:8082/actuator/prometheus | Select-String "kafka_producer"

# Alert Engine metrics
curl http://localhost:8083/actuator/prometheus | Select-String "kafka_consumer"
```

## Expected Behavior

### When Kp-index < 4.0 (No Alert)
- Data Collector publishes event to Kafka
- Alert Engine consumes event
- **No alert created** (below threshold)
- Logs: "Kp-index X.XX is below alert threshold 4.0"

### When 4.0 <= Kp-index < 5.0 (MINOR)
- Alert created with severity: **MINOR**
- Saved to `alerts` table
- Published to Kafka topic: `alerts.warning`

### When 5.0 <= Kp-index < 6.0 (MODERATE)
- Alert created with severity: **MODERATE**
- Published to: `alerts.warning`

### When 6.0 <= Kp-index < 7.0 (STRONG)
- Alert created with severity: **STRONG**
- Published to: `alerts.warning`

### When 7.0 <= Kp-index < 8.0 (SEVERE)
- Alert created with severity: **SEVERE** 🚨
- Published to: `alerts.critical`

### When Kp-index >= 8.0 (EXTREME)
- Alert created with severity: **EXTREME** 🚨🚨
- Published to: `alerts.critical`

## Troubleshooting

### Alert Engine not consuming messages
```powershell
# Check Kafka consumer group
docker exec -it ai-kafka kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group alert-engine-group
```

### Database connection failed
```powershell
# Test PostgreSQL connection
docker exec -it ai-postgres psql -U ai_user -d ai_scientist -c "SELECT 1;"
```

### Eureka registration failed
```powershell
# Check Eureka Server logs
docker logs ai-eureka-server | Select-String "ALERT-ENGINE"
```

## Quick Test Commands

```powershell
# 1. Start both services (in separate terminals)
# Terminal 1: data-collector
# Terminal 2: alert-engine

# 2. Trigger collection
curl -X POST http://localhost:8082/api/v1/collector/collect/kp-index

# 3. Wait 5 seconds, then check alerts
Start-Sleep -Seconds 5
curl http://localhost:8083/api/v1/alerts | ConvertFrom-Json | Format-Table

# 4. Check critical alerts
curl http://localhost:8083/api/v1/alerts/critical | ConvertFrom-Json
```

## Success Criteria

✅ Alert Engine starts without errors
✅ Registers with Eureka as "ALERT-ENGINE"
✅ Consumes Kp-index events from Kafka
✅ Creates alerts when Kp >= 4.0
✅ Publishes critical alerts (Kp >= 7.0) to `alerts.critical`
✅ Saves alerts to PostgreSQL `alerts` table
✅ REST API returns alerts correctly
✅ No exceptions in logs
