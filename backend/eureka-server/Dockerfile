# Base image you requested (already cached in your machine)
FROM amazoncorretto:25-alpine3.22-jdk

# Create non-root user (production-ish hardening)
RUN addgroup -S app && adduser -S app -G app

# Copy jar
ADD target/*.jar /home/app/app.jar
WORKDIR /home/app

# Expose internal port
EXPOSE 8761

# Spring profile and JVM tuning hooks (override via compose env if needed)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=docker

USER app

# Actuator health endpoint assumed. If not present, we'll adjust later.
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
