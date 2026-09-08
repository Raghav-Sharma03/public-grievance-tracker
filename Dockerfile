FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src/ src/
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN mkdir -p /app/uploads/complaints \
    && chown -R 10001:10001 /app

COPY --from=build --chown=10001:10001 \
    /workspace/target/grievance-tracker-0.0.1-SNAPSHOT.jar \
    /app/grievance-tracker.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx256m", "-jar", "/app/grievance-tracker.jar"]
