FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY credit-recovery-domain/pom.xml credit-recovery-domain/pom.xml
COPY credit-strategy-api/pom.xml credit-strategy-api/pom.xml
COPY credit-strategy-processor/pom.xml credit-strategy-processor/pom.xml

COPY credit-recovery-domain/src credit-recovery-domain/src
COPY credit-strategy-processor/src credit-strategy-processor/src

RUN mvn -pl credit-strategy-processor -am -DskipTests package

FROM eclipse-temurin:21-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --system --uid 10001 --create-home appuser

WORKDIR /app
COPY --from=build /workspace/credit-strategy-processor/target/credit-strategy-processor-1.0.0-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
