FROM amazoncorretto:25-alpine3.22-jdk

# ✅ Non-root user for security
RUN addgroup -S app && adduser -S app -G app

# ✅ Copy jar
ADD target/*.jar /home/app/app.jar
WORKDIR /home/app

EXPOSE 8071

# ✅ JVM + profile (overridable by docker-compose)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=docker

USER app

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
