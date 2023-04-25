FROM openjdk:17-alpine

COPY target/TeamPlaningToolBackend-0.0.1-SNAPSHOT.jar app.jar

ARG BUILD_DIR=${pwr}

COPY ${BUILD_DIR} assets

ENTRYPOINT ["java", "-jar", "/app.jar"]
