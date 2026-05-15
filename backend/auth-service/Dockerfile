# ✅ Base image (same as your emp-service)
FROM amazoncorretto:25-alpine3.22-jdk

# ✅ Create non-root user (security)
RUN addgroup -S app && adduser -S app -G app

# ✅ Copy jar file
ADD target/*.jar /home/app/app.jar

# ✅ Set working directory
WORKDIR /home/app

# ✅ Expose port (internal)
EXPOSE 8191

# ✅ Default env (can override in docker-compose)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=docker

# ✅ Run as non-root user
USER app

# ✅ Run application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
