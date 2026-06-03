FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/java-cicd-demo-1.0.jar app.jar

EXPOSE 8080

#ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["sleep", "infinity"]