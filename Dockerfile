FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="AI Scientist Ecosystem"
LABEL description="Alert Engine - Detects and processes space weather alerts"

WORKDIR /app

# Copy JAR file
COPY target/alert-engine-1.0.0.jar app.jar

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /app

USER appuser

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-jar", \
    "app.jar"]
