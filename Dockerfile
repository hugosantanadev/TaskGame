# syntax=docker/dockerfile:1
#
# Imagem única para publicar o GasmTask num serviço de contêiner (Railway, Render, Fly.io, Koyeb...):
# o Spring Boot serve a API e o PWA na mesma origem, sem Nginx. Para rodar tudo na própria máquina
# com Docker, use o docker-compose.yml (Nginx + API + PostgreSQL).
#
# Variáveis no serviço: DB_URL, DB_USER, DB_PASSWORD, JWT_SECRET e AUTH_COOKIE_SECURE=true.
# PORT é lida automaticamente. Para as fotos de prova sobreviverem a novas versões, monte um volume em /app/data.

# ---- app (PWA) ----
FROM node:24-alpine AS web
WORKDIR /web
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- API com o app dentro ----
FROM eclipse-temurin:25-jdk AS api
WORKDIR /workspace
COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw
COPY backend/src/ src/
COPY --from=web /web/dist/ src/main/resources/static/
RUN ./mvnw -B -q -DskipTests package

# ---- runtime ----
FROM eclipse-temurin:25-jre
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --system --uid 10001 --create-home app
WORKDIR /app
COPY --from=api /workspace/target/app.jar app.jar
RUN mkdir -p /app/data && chown app:app /app/data
USER app
ENV STORAGE_DIR=/app/data
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=10 \
  CMD curl -fsS "http://localhost:${PORT:-8080}/actuator/health" || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
