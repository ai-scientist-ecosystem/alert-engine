# AI Scientist Ecosystem - Infrastructure

This directory contains the core infrastructure components for the AI Scientist Ecosystem microservices architecture.

## Components

### Service Discovery - Eureka Server
Located in [`eureka-server/`](./eureka-server)

Netflix Eureka Server provides service registration and discovery. All microservices register with Eureka, enabling:
- Dynamic service discovery
- Load balancing
- Health monitoring
- Fault tolerance

**Port:** 8761

### Configuration Management - Config Server
Located in [`config-server/`](./config-server)

Spring Cloud Config Server provides centralized, versioned configuration management:
- Git-backed configuration repository
- Environment-specific configurations
- Encryption/decryption of sensitive properties
- Dynamic configuration refresh

**Port:** 8888

### Supporting Services

The `docker-compose.yml` file includes additional supporting services:

| Service | Port | Purpose |
|---------|------|---------|
| Kafka | 9092 | Message broker for event-driven architecture |
| Redis | 6379 | Caching and session storage |
| PostgreSQL | 5432 | Primary database |

## Quick Start

### Start All Infrastructure Services

```bash
# Start all services in the background
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f
```

### Start Only Core Services

```bash
# Start just Eureka and Config Server
docker-compose up -d eureka-server config-server

# Start with supporting services
docker-compose up -d eureka-server config-server kafka redis postgres
```

### Access Dashboards

| Service | URL | Credentials |
|---------|-----|-------------|
| Eureka Dashboard | http://localhost:8761 | admin/admin |
| Config Server | http://localhost:8888 | admin/admin |

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    AI Scientist Ecosystem                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │   Eureka    │    │   Config    │    │     API     │         │
│  │   Server    │◄───│   Server    │◄───│   Gateway   │         │
│  │   :8761     │    │   :8888     │    │   :8080     │         │
│  └──────┬──────┘    └─────────────┘    └──────┬──────┘         │
│         │                                      │                 │
│         │         ┌─────────────────────────────┼───────────┐   │
│         │         │                             │           │   │
│         ▼         ▼                             ▼           ▼   │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌────────────┐│
│  │    Data     │ │    Alert    │ │     AI      │ │   Alert    ││
│  │  Collector  │ │   Engine    │ │   Agents    │ │  Publisher ││
│  │   :8081     │ │   :8082     │ │   :8083     │ │   :8084    ││
│  └─────────────┘ └─────────────┘ └─────────────┘ └────────────┘│
│         │              │              │              │          │
│         └──────────────┴──────────────┴──────────────┘          │
│                              │                                   │
│                    ┌─────────┴─────────┐                        │
│                    ▼                   ▼                        │
│             ┌─────────────┐    ┌─────────────┐                  │
│             │    Kafka    │    │    Redis    │                  │
│             │   :9092     │    │   :6379     │                  │
│             └─────────────┘    └─────────────┘                  │
│                    │                                            │
│                    ▼                                            │
│             ┌─────────────┐                                     │
│             │  PostgreSQL │                                     │
│             │   :5432     │                                     │
│             └─────────────┘                                     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Configuration

### Environment Variables

Create a `.env` file for local development:

```bash
# Eureka Server
EUREKA_USER=admin
EUREKA_PASSWORD=admin

# Config Server
CONFIG_USER=admin
CONFIG_PASSWORD=admin
CONFIG_GIT_URI=https://github.com/ai-scientist-ecosystem/config-repo
ENCRYPT_KEY=your-strong-encryption-key

# Database
POSTGRES_PASSWORD=your-strong-password
```

### Client Service Configuration

Each microservice should include a `bootstrap.yml` to connect to Config Server:

```yaml
spring:
  application:
    name: your-service-name
  cloud:
    config:
      uri: http://localhost:8888
      username: admin
      password: admin
      fail-fast: true
```

And `application.yml` for Eureka registration:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://admin:admin@localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## Development

### Building Individual Services

```bash
# Build Eureka Server
cd eureka-server
./mvnw clean package

# Build Config Server
cd config-server
./mvnw clean package
```

### Running Locally Without Docker

```bash
# Start Eureka Server (terminal 1)
cd eureka-server
./mvnw spring-boot:run

# Start Config Server (terminal 2)
cd config-server
./mvnw spring-boot:run
```

## Production Considerations

### High Availability

For production deployments:

1. **Eureka Server**: Run 3+ instances that peer with each other
2. **Config Server**: Run 2+ instances behind a load balancer
3. **Kafka**: Deploy as a cluster with replication factor ≥ 3
4. **Redis**: Use Redis Cluster or Sentinel for HA
5. **PostgreSQL**: Use managed service or configure streaming replication

### Security

1. Change default passwords via environment variables
2. Use strong encryption keys for Config Server
3. Enable TLS for all inter-service communication
4. Use secrets management (HashiCorp Vault, AWS Secrets Manager)

### Monitoring

Consider adding:
- Prometheus + Grafana for metrics
- ELK Stack or Loki for logs
- Jaeger or Zipkin for distributed tracing

## Related Repositories

- [data-collector](https://github.com/ai-scientist-ecosystem/data-collector) - Data ingestion service
- [alert-engine](https://github.com/ai-scientist-ecosystem/alert-engine) - Alert processing service
- [api-gateway](https://github.com/ai-scientist-ecosystem/api-gateway) - API gateway service
- [config-repo](https://github.com/ai-scientist-ecosystem/config-repo) - Configuration repository

## Troubleshooting

### Eureka Server Not Starting
- Check if port 8761 is available
- Verify Java 17+ is installed

### Config Server Connection Issues
- Ensure Eureka Server is running and healthy
- Check Git repository URL and credentials
- Verify network connectivity

### Services Not Registering
- Check Eureka client configuration in services
- Verify network connectivity to Eureka Server
- Check application logs for errors
