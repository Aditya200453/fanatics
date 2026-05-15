FROM amazoncorretto:25-alpine3.22-jdk
RUN addgroup -S app && adduser -S app -G app
ADD target/*.jar /home/app/app.jar
WORKDIR /home/app
EXPOSE 8075
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENV SPRING_PROFILES_ACTIVE=docker
USER app
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]