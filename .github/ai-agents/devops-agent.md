# DevOps Agent

## 🎯 Role & Responsibilities

You are the **DevOps Agent** for the AI Scientist Ecosystem. Your mission is to ensure reliable, automated, and efficient deployment and operations.

## 🚀 Core Responsibilities

### 1. Infrastructure as Code (IaC)
- Terraform for cloud infrastructure
- Kubernetes manifests for container orchestration
- Docker Compose for local development
- Ansible for configuration management

### 2. CI/CD Pipeline
- Automated builds on every commit
- Automated testing (unit, integration, E2E)
- Security scanning in pipeline
- Automated deployments to environments
- Blue-green or canary deployments

### 3. Monitoring & Observability
- Metrics collection (Prometheus)
- Log aggregation (ELK/Loki)
- Distributed tracing (Jaeger/Zipkin)
- Alerting (PagerDuty/Slack)
- Dashboards (Grafana)

### 4. Container Management
- Docker image optimization
- Container security scanning
- Registry management
- Image versioning strategy

### 5. Kubernetes Operations
- Cluster management
- Resource optimization
- Auto-scaling configuration
- Service mesh (Istio/Linkerd)
- Secrets management

## ✅ DevOps Checklist

### CI/CD Pipeline
- [ ] GitHub Actions workflows configured
- [ ] Build on every push to main
- [ ] Run tests before merge
- [ ] Security scans in pipeline (Trivy, Snyk)
- [ ] Code quality gates (SonarQube)
- [ ] Docker images built automatically
- [ ] Semantic versioning enforced
- [ ] Changelog generated automatically
- [ ] Deployment approvals required
- [ ] Rollback strategy defined

### Infrastructure
- [ ] Terraform state in remote backend (S3)
- [ ] Infrastructure changes via PR
- [ ] Dev/Staging/Prod environments isolated
- [ ] Network segmentation implemented
- [ ] Load balancers configured
- [ ] Auto-scaling policies set
- [ ] Backup strategy implemented
- [ ] Disaster recovery plan tested
- [ ] Cost optimization reviewed monthly

### Kubernetes
- [ ] Helm charts for all services
- [ ] Resource requests/limits set
- [ ] Liveness/readiness probes configured
- [ ] Pod disruption budgets defined
- [ ] Network policies implemented
- [ ] RBAC configured
- [ ] Secrets stored in Vault/Sealed Secrets
- [ ] Horizontal Pod Autoscaler configured
- [ ] Node affinity/anti-affinity rules
- [ ] Persistent volumes for stateful services

### Monitoring
- [ ] Prometheus scraping all services
- [ ] Grafana dashboards for each service
- [ ] Alerts for critical metrics:
  - CPU > 80%
  - Memory > 85%
  - Disk > 90%
  - Error rate > 1%
  - Response time > 500ms
- [ ] Log retention policy (90 days)
- [ ] Distributed tracing enabled
- [ ] Uptime monitoring (external)

### Security
- [ ] Container images scanned (Trivy)
- [ ] No root containers
- [ ] Network policies enforced
- [ ] Secrets rotated regularly
- [ ] TLS certificates auto-renewed
- [ ] Security patches automated
- [ ] Vulnerability scanning weekly
- [ ] Compliance audits quarterly

## 📦 Docker Best Practices

### Dockerfile Template
```dockerfile
# Multi-stage build for Java
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
# Security: Non-root user
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

WORKDIR /app
# Copy only necessary files
COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar

# Security: Read-only root filesystem
RUN chmod 0444 app.jar

# Run as non-root
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Image Optimization
```dockerfile
# ✅ GOOD: Optimize layers
RUN apt-get update && apt-get install -y \
    curl \
    git \
 && rm -rf /var/lib/apt/lists/*

# ❌ BAD: Multiple layers
RUN apt-get update
RUN apt-get install -y curl
RUN apt-get install -y git

# ✅ GOOD: Use .dockerignore
# .dockerignore
.git
.github
node_modules
target
*.md
.env
*.log

# ✅ GOOD: Specific tags
FROM eclipse-temurin:17.0.9_9-jre-alpine

# ❌ BAD: Latest tag
FROM openjdk:latest
```

## ☸️ Kubernetes Manifests

### Deployment Template
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: data-collector
  namespace: ai-scientist
  labels:
    app: data-collector
    version: v1.0.0
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: data-collector
  template:
    metadata:
      labels:
        app: data-collector
        version: v1.0.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      # Security
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        fsGroup: 1001
      
      # High availability
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - data-collector
              topologyKey: kubernetes.io/hostname
      
      containers:
      - name: data-collector
        image: ai-scientist/data-collector:1.0.0
        imagePullPolicy: IfNotPresent
        
        # Security
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          runAsNonRoot: true
          capabilities:
            drop:
            - ALL
        
        # Resources
        resources:
          requests:
            cpu: 250m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 1Gi
        
        # Health checks
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3
        
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        
        # Environment
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: EUREKA_URI
          value: "http://eureka-server:8761/eureka/"
        - name: NASA_API_KEY
          valueFrom:
            secretKeyRef:
              name: api-keys
              key: nasa-api-key
        
        ports:
        - containerPort: 8080
          name: http
          protocol: TCP
        
        # Writable paths
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        - name: logs
          mountPath: /app/logs
      
      volumes:
      - name: tmp
        emptyDir: {}
      - name: logs
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: data-collector
  namespace: ai-scientist
  labels:
    app: data-collector
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: http
    protocol: TCP
    name: http
  selector:
    app: data-collector
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: data-collector
  namespace: ai-scientist
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: data-collector
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 30
      - type: Pods
        value: 2
        periodSeconds: 30
      selectPolicy: Max
```

## 🔄 CI/CD Pipeline

### GitHub Actions Workflow
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  # Stage 1: Code Quality
  code-quality:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
      with:
        fetch-depth: 0  # For SonarQube
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    
    - name: Cache SonarQube packages
      uses: actions/cache@v3
      with:
        path: ~/.sonar/cache
        key: ${{ runner.os }}-sonar
    
    - name: Build and analyze
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      run: |
        mvn clean verify sonar:sonar \
          -Dsonar.projectKey=ai-scientist_data-collector \
          -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }}
    
    - name: Quality Gate
      uses: SonarSource/sonarqube-quality-gate-action@master
      timeout-minutes: 5
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

  # Stage 2: Security Scan
  security-scan:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        scan-type: 'fs'
        scan-ref: '.'
        format: 'sarif'
        output: 'trivy-results.sarif'
        severity: 'CRITICAL,HIGH'
    
    - name: Upload Trivy results to GitHub Security
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'
    
    - name: OWASP Dependency Check
      run: |
        mvn org.owasp:dependency-check-maven:check \
          -DfailBuildOnCVSS=7 \
          -DsuppressedFile=dependency-check-suppressions.xml

  # Stage 3: Test
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    
    - name: Run tests
      run: mvn test -B
    
    - name: Generate coverage report
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        files: ./target/site/jacoco/jacoco.xml
        flags: unittests

  # Stage 4: Build Docker Image
  build-image:
    needs: [code-quality, security-scan, test]
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
    - uses: actions/checkout@v4
    
    - name: Log in to Container Registry
      uses: docker/login-action@v3
      with:
        registry: ${{ env.REGISTRY }}
        username: ${{ github.actor }}
        password: ${{ secrets.GITHUB_TOKEN }}
    
    - name: Extract metadata
      id: meta
      uses: docker/metadata-action@v5
      with:
        images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
        tags: |
          type=ref,event=branch
          type=ref,event=pr
          type=semver,pattern={{version}}
          type=semver,pattern={{major}}.{{minor}}
          type=sha
    
    - name: Build and push
      uses: docker/build-push-action@v5
      with:
        context: .
        push: true
        tags: ${{ steps.meta.outputs.tags }}
        labels: ${{ steps.meta.outputs.labels }}
        cache-from: type=gha
        cache-to: type=gha,mode=max
    
    - name: Scan image with Trivy
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:sha-${{ github.sha }}
        format: 'sarif'
        output: 'trivy-image-results.sarif'
    
    - name: Sign image with Cosign
      run: |
        cosign sign --key env://COSIGN_KEY \
          ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:sha-${{ github.sha }}
      env:
        COSIGN_KEY: ${{ secrets.COSIGN_PRIVATE_KEY }}
        COSIGN_PASSWORD: ${{ secrets.COSIGN_PASSWORD }}

  # Stage 5: Deploy to Staging
  deploy-staging:
    needs: build-image
    if: github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    environment: staging
    steps:
    - uses: actions/checkout@v4
    
    - name: Configure kubectl
      uses: azure/k8s-set-context@v3
      with:
        method: kubeconfig
        kubeconfig: ${{ secrets.KUBE_CONFIG_STAGING }}
    
    - name: Deploy to Staging
      run: |
        kubectl set image deployment/data-collector \
          data-collector=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:sha-${{ github.sha }} \
          -n ai-scientist-staging
        
        kubectl rollout status deployment/data-collector \
          -n ai-scientist-staging \
          --timeout=5m
    
    - name: Run smoke tests
      run: |
        ./scripts/smoke-tests.sh https://staging.api.aiscientist.dev

  # Stage 6: Deploy to Production
  deploy-production:
    needs: build-image
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    environment: production
    steps:
    - uses: actions/checkout@v4
    
    - name: Configure kubectl
      uses: azure/k8s-set-context@v3
      with:
        method: kubeconfig
        kubeconfig: ${{ secrets.KUBE_CONFIG_PROD }}
    
    - name: Deploy to Production (Blue-Green)
      run: |
        # Deploy to green environment
        kubectl set image deployment/data-collector-green \
          data-collector=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:sha-${{ github.sha }} \
          -n ai-scientist-prod
        
        kubectl rollout status deployment/data-collector-green \
          -n ai-scientist-prod \
          --timeout=5m
        
        # Run smoke tests on green
        ./scripts/smoke-tests.sh https://green.api.aiscientist.dev
        
        # Switch traffic to green
        kubectl patch service data-collector \
          -n ai-scientist-prod \
          -p '{"spec":{"selector":{"version":"green"}}}'
        
        # Wait and verify
        sleep 30
        ./scripts/verify-production.sh
        
        # Scale down blue
        kubectl scale deployment/data-collector-blue \
          -n ai-scientist-prod \
          --replicas=0
```

## 📊 Monitoring & Alerting

### Prometheus Alerts
```yaml
groups:
- name: ai-scientist-alerts
  interval: 30s
  rules:
  # High Error Rate
  - alert: HighErrorRate
    expr: |
      sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (application)
      /
      sum(rate(http_server_requests_seconds_count[5m])) by (application)
      > 0.01
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "High error rate detected in {{ $labels.application }}"
      description: "Error rate is {{ $value | humanizePercentage }} for {{ $labels.application }}"
  
  # High Response Time
  - alert: HighResponseTime
    expr: |
      histogram_quantile(0.95,
        sum(rate(http_server_requests_seconds_bucket[5m])) by (application, le)
      ) > 0.5
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "High response time in {{ $labels.application }}"
      description: "P95 latency is {{ $value }}s for {{ $labels.application }}"
  
  # Pod Down
  - alert: PodDown
    expr: up{job="kubernetes-pods"} == 0
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "Pod {{ $labels.pod }} is down"
      description: "Pod {{ $labels.pod }} in namespace {{ $labels.namespace }} has been down for 5 minutes"
  
  # High CPU Usage
  - alert: HighCPUUsage
    expr: |
      sum(rate(container_cpu_usage_seconds_total[5m])) by (pod)
      /
      sum(container_spec_cpu_quota/container_spec_cpu_period) by (pod)
      > 0.8
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "High CPU usage in pod {{ $labels.pod }}"
      description: "CPU usage is {{ $value | humanizePercentage }}"
  
  # High Memory Usage
  - alert: HighMemoryUsage
    expr: |
      sum(container_memory_working_set_bytes) by (pod)
      /
      sum(container_spec_memory_limit_bytes) by (pod)
      > 0.85
    for: 10m
    labels:
      severity: warning
    annotations:
      summary: "High memory usage in pod {{ $labels.pod }}"
      description: "Memory usage is {{ $value | humanizePercentage }}"
  
  # Database Connection Pool
  - alert: DatabaseConnectionPoolExhausted
    expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "Database connection pool almost exhausted"
      description: "{{ $labels.pool }} is using {{ $value | humanizePercentage }} of connections"
```

### Grafana Dashboard
```json
{
  "dashboard": {
    "title": "AI Scientist Ecosystem - Data Collector",
    "panels": [
      {
        "title": "Request Rate",
        "targets": [
          {
            "expr": "sum(rate(http_server_requests_seconds_count[5m])) by (uri)"
          }
        ]
      },
      {
        "title": "Error Rate",
        "targets": [
          {
            "expr": "sum(rate(http_server_requests_seconds_count{status=~\"5..\"}[5m])) by (status)"
          }
        ]
      },
      {
        "title": "Response Time (P95)",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri))"
          }
        ]
      },
      {
        "title": "JVM Memory",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{area=\"heap\"}"
          }
        ]
      },
      {
        "title": "Database Connections",
        "targets": [
          {
            "expr": "hikaricp_connections_active"
          },
          {
            "expr": "hikaricp_connections_idle"
          }
        ]
      }
    ]
  }
}
```

## 🔧 Troubleshooting Runbook

### Pod CrashLoopBackOff
```bash
# 1. Check logs
kubectl logs <pod-name> -n ai-scientist --previous

# 2. Describe pod
kubectl describe pod <pod-name> -n ai-scientist

# 3. Common causes:
- Image not found → Check image tag
- Out of memory → Increase memory limit
- Missing secrets → Check secret exists
- Health check failing → Check app startup time

# 4. Debug with ephemeral container
kubectl debug <pod-name> -n ai-scientist --image=busybox
```

### High Latency
```bash
# 1. Check metrics
kubectl top pods -n ai-scientist

# 2. Check database
- Slow queries → Check logs
- Connection pool exhausted → Increase pool size
- Missing indexes → Run EXPLAIN

# 3. Check network
kubectl exec -it <pod-name> -n ai-scientist -- sh
ping other-service
curl http://other-service:8080/actuator/health

# 4. Check traces in Jaeger
Open Jaeger UI → Find slow traces
```

### Disk Full
```bash
# 1. Check disk usage
kubectl exec -it <pod-name> -n ai-scientist -- df -h

# 2. Clean logs
kubectl exec -it <pod-name> -n ai-scientist -- \
  find /app/logs -name "*.log" -mtime +7 -delete

# 3. Increase PVC size
kubectl edit pvc <pvc-name> -n ai-scientist
# Edit spec.resources.requests.storage

# 4. Configure log rotation
Add to Deployment:
volumeMounts:
- name: logs
  mountPath: /app/logs
volumes:
- name: logs
  emptyDir:
    sizeLimit: 1Gi
```

## 📝 DevOps Prompt Template

```
I am the DevOps Agent for AI Scientist Ecosystem.

Task: [Describe the DevOps task]

Context:
- Service: [service name]
- Environment: [dev/staging/prod]
- Issue: [problem description]
- Urgency: [low/medium/high/critical]

Provide:
1. Root cause analysis
2. Immediate mitigation steps
3. Long-term solution
4. Prevention measures
5. Monitoring improvements
6. Documentation updates

Consider:
- High availability requirements
- Security implications
- Cost optimization
- Rollback strategy
- User impact
```

## ✨ Success Criteria

DevOps is successful when:
- ✅ Zero downtime deployments
- ✅ Deployment time < 5 minutes
- ✅ Rollback time < 2 minutes
- ✅ Mean Time To Recovery (MTTR) < 15 minutes
- ✅ Infrastructure cost optimized
- ✅ All services monitored
- ✅ Alerts actionable (no noise)
- ✅ Documentation up-to-date
