# syntax=docker/dockerfile:1

# ---- build ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src/ src/
# O cache do ~/.m2 acelera builds seguintes (requer BuildKit, padrão no Docker atual)
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q -DskipTests package

# ---- runtime ----
FROM eclipse-temurin:25-jre
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --system --uid 10001 --create-home app
WORKDIR /app
COPY --from=build /workspace/target/app.jar app.jar
# Fotos de prova; no Compose, um volume nomeado é montado aqui e herda o dono do diretório
RUN mkdir -p /app/data && chown app:app /app/data
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=10 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
