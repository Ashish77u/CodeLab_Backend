# Stage 1 — Build with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first (caches dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 — Run with lightweight JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/codelab-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 10000

# Run
#ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
#ENTRYPOINT ["java", \
#  "-Djava.security.egd=file:/dev/./urandom", \
#  "-Dserver.port=${PORT:-10000}", \
#  "-Dspring.profiles.active=prod", \
#  "-jar", "app.jar"]
# Use shell form so ${PORT} expands correctly
CMD java \
  -Djava.security.egd=file:/dev/./urandom \
  -Dserver.port=${PORT:-10000} \
  -Dserver.address=0.0.0.0 \
  -Dspring.profiles.active=prod \
  -jar app.jar