FROM eclipse-temurin:21-jdk-alpine AS build

# Installer Maven
RUN apk add --no-cache maven

WORKDIR /app

# Copier tous les fichiers du projet
COPY . .

# Build le projet parent et tous les modules
RUN mvn clean install -DskipTests

# Image de production
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Installer bash pour le script de démarrage
RUN apk add --no-cache bash

# Copier les JARs construits
COPY --from=build /app/frontoffice/target/*.jar frontoffice.jar
COPY --from=build /app/backoffice/target/*.jar backoffice.jar
COPY --from=build /app/frontrest/target/*.jar frontrest.jar
# Copier le script de démarrage
COPY docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8080 8081 8082

ENTRYPOINT ["/app/docker-entrypoint.sh"]
