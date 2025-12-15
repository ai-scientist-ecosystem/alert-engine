# Config Server

Centralized configuration server for the AI Scientist Ecosystem microservices architecture.

## Overview

This module provides Spring Cloud Config Server for centralized, versioned configuration management. Features include:

- Git-backed configuration repository
- Environment-specific configurations (dev, staging, prod)
- Encryption/decryption of sensitive properties
- Dynamic configuration refresh
- Service discovery integration via Eureka

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Git (for configuration repository)

### Local Development

```bash
# Build the project
./mvnw clean package

# Run with native profile (local file system)
./mvnw spring-boot:run -Dspring.profiles.active=native

# Run with git profile (default)
./mvnw spring-boot:run
```

The Config Server will be available at: http://localhost:8888

### Docker

```bash
# Build the image
docker build -t ai-scientist/config-server .

# Run the container
docker run -p 8888:8888 \
  -e CONFIG_USER=admin \
  -e CONFIG_PASSWORD=secret \
  -e CONFIG_GIT_URI=https://github.com/ai-scientist-ecosystem/config-repo \
  -e EUREKA_URI=http://admin:admin@eureka-server:8761/eureka/ \
  ai-scientist/config-server
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `CONFIG_USER` | Username for Config Server | admin |
| `CONFIG_PASSWORD` | Password for Config Server | admin |
| `CONFIG_GIT_URI` | Git repository URL for configurations | https://github.com/ai-scientist-ecosystem/config-repo |
| `CONFIG_GIT_BRANCH` | Git branch to use | main |
| `CONFIG_PROFILE` | Active profile (git or native) | git |
| `CONFIG_NATIVE_PATH` | Path for native profile configs | classpath:/configs |
| `ENCRYPT_KEY` | Encryption key for sensitive properties | changeme |
| `EUREKA_ENABLED` | Enable Eureka registration | true |
| `EUREKA_URI` | Eureka server URL | http://admin:admin@localhost:8761/eureka/ |

### Configuration Repository Structure

```
config-repo/
├── application.yml           # Shared configuration for all services
├── application-dev.yml       # Shared dev configuration
├── application-prod.yml      # Shared prod configuration
├── data-collector/
│   ├── application.yml       # data-collector specific config
│   └── application-prod.yml  # data-collector prod config
├── alert-engine/
│   └── application.yml       # alert-engine specific config
├── api-gateway/
│   └── application.yml       # api-gateway specific config
└── shared/
    └── kafka.yml             # Shared Kafka configuration
```

### Client Configuration

To connect a microservice to this Config Server, add:

```yaml
# bootstrap.yml in client service
spring:
  application:
    name: data-collector  # Must match config folder name
  cloud:
    config:
      uri: http://localhost:8888
      username: admin
      password: admin
      fail-fast: true
```

Add the Config Client dependency:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /{application}/{profile}` | Get configuration for application and profile |
| `GET /{application}/{profile}/{label}` | Get configuration with specific Git label |
| `POST /encrypt` | Encrypt a value |
| `POST /decrypt` | Decrypt a value |
| `GET /actuator/health` | Health check |

### Example Requests

```bash
# Get configuration for data-collector in dev profile
curl -u admin:admin http://localhost:8888/data-collector/dev

# Encrypt a secret
curl -u admin:admin -X POST http://localhost:8888/encrypt -d "my-secret"

# Decrypt a secret
curl -u admin:admin -X POST http://localhost:8888/decrypt -d "{cipher}encrypted-value"
```

## Encryption

To use encrypted properties in configuration files:

1. Encrypt the value using the `/encrypt` endpoint
2. Use the encrypted value with `{cipher}` prefix:

```yaml
database:
  password: '{cipher}AQBhZ3J5cHRpb24...'
```

**Important**: In production, use a strong encryption key and consider using asymmetric encryption.

## High Availability

For production, run multiple Config Server instances behind a load balancer:

```yaml
# In each instance
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

Clients can discover Config Server via Eureka:

```yaml
# In client bootstrap.yml
spring:
  cloud:
    config:
      discovery:
        enabled: true
        service-id: config-server
```

## Related Services

- [Eureka Server](../eureka-server) - Service discovery
- [API Gateway](../../api-gateway) - API routing and load balancing
