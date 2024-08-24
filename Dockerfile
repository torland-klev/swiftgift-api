ARG JAR_FILE
FROM amazoncorretto:22-headless
COPY $JAR_FILE $JAR_FILE
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "$JAR_FILE"]