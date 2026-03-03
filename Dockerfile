# ── Stage 1: Build React frontend ─────────────────────────────────────────────
FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci --prefer-offline

COPY frontend/ ./
RUN npm run build

# ── Stage 2: Build Spring Boot application ────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS backend-builder

WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy pom first (layer cache)
COPY pom.xml ./
RUN mvn dependency:go-offline -B -q 2>/dev/null || true

# Copy source
COPY src/ src/

# Copy built frontend to where maven-resources-plugin expects it
COPY --from=frontend-builder /app/frontend/dist frontend/dist

# Build the jar, skip frontend-maven-plugin (already built) and tests
RUN mvn package -DskipTests -Dfrontend.skip=true -B -q

# ── Stage 3: Runtime image ───────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=backend-builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=docker", \
  "-jar", "app.jar"]
