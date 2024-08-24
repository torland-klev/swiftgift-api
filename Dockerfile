FROM amazoncorretto:22-headless
COPY swiftgift-api.jar swiftgift-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/swiftgift-api.jar"]