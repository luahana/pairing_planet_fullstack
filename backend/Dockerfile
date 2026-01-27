# Builder stage - use standard JDK (not Alpine)
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew clean build -x test --no-daemon

# Runner stage - use standard Debian-based JRE (not Alpine)
# webp4j bundles native libraries compiled against glibc, which Alpine's musl doesn't support
FROM eclipse-temurin:21-jre AS runner

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder ./app/build/libs/*.jar ./app.jar

EXPOSE 4000

ENTRYPOINT ["java", "-Xms256m", "-Xmx768m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-jar", "app.jar"]
