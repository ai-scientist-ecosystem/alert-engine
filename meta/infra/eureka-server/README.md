# Eureka Server

Service Discovery server for the AI Scientist Ecosystem microservices architecture.

## Overview

This module provides Netflix Eureka Server for service registration and discovery. All microservices in the ecosystem register themselves with this server, enabling:

- Dynamic service discovery
- Load balancing
- Fault tolerance
- Health monitoring

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Local Development

```bash
# Build the project
./mvnw clean package

# Run the server
./mvnw spring-boot:run
```

The Eureka dashboard will be available at: http://localhost:8761

### Docker

```bash
# Build the image
docker build -t ai-scientist/eureka-server .

# Run the container
docker run -p 8761:8761 \
  -e EUREKA_USER=admin \
  -e EUREKA_PASSWORD=secret \
  ai-scientist/eureka-server
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `EUREKA_USER` | Username for Eureka dashboard | admin |
| `EUREKA_PASSWORD` | Password for Eureka dashboard | admin |
| `EUREKA_HOSTNAME` | Hostname for the Eureka server | localhost |
| `EUREKA_SELF_PRESERVATION` | Enable self-preservation mode | false |

### Client Configuration

To register a microservice with this Eureka server, add the following to the service's configuration:

```yaml
# application.yml in client service
eureka:
  client:
    service-url:
      defaultZone: http://admin:admin@localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

Add the Eureka client dependency:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

## API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /` | Eureka dashboard |
| `GET /eureka/apps` | List all registered applications |
| `GET /eureka/apps/{appId}` | Get specific application info |
| `GET /actuator/health` | Health check |

## Security

The Eureka dashboard and registry endpoints are protected with HTTP Basic authentication. Health and info endpoints are publicly accessible for container orchestration tools.

## High Availability

For production, run multiple Eureka server instances that peer with each other:

```yaml
# eureka-server-1
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka-server-2:8761/eureka/

# eureka-server-2
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka-server-1:8761/eureka/
```

## Related Services

- [Config Server](../config-server) - Centralized configuration management
- [API Gateway](../../api-gateway) - API routing and load balancing
