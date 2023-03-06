FROM openjdk:11-jdk-slim
ENV SPRING_PROFILES_ACTIVE=dev
COPY service/build/libs/asset-management-service-0.1.0-SNAPSHOT.jar /app.jar
CMD ["java", "-jar", "/app.jar"]

