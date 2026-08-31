FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY spi/event-listener/pom.xml .
RUN mvn dependency:go-offline

COPY spi/event-listener/src ./src
RUN mvn clean package -DskipTests

FROM quay.io/keycloak/keycloak:26.2.0 AS runtime

COPY --from=build /build/target/event-listener*.jar /opt/keycloak/providers/event-listener.jar

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]