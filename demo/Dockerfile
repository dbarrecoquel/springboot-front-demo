FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Installer Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Copier seulement les pom.xml pour profiter du cache Docker
COPY pom.xml .
COPY product/pom.xml product/pom.xml
COPY catalog/pom.xml catalog/pom.xml
COPY user/pom.xml user/pom.xml
COPY address/pom.xml address/pom.xml
COPY shopping/pom.xml shopping/pom.xml
COPY shipping-method/pom.xml shipping-method/pom.xml
COPY events/pom.xml events/pom.xml
COPY order/pom.xml order/pom.xml
COPY frontoffice/pom.xml frontoffice/pom.xml
COPY backoffice/pom.xml backoffice/pom.xml
COPY frontrest/pom.xml frontrest/pom.xml

# Télécharger les dépendances avec retry
RUN mvn dependency:go-offline -DskipTests || \
    mvn dependency:resolve -DskipTests || \
    mvn dependency:resolve-plugins -DskipTests || \
    true

# Copier le code source
COPY product product
COPY catalog catalog
COPY user user
COPY address address
COPY shopping shopping
COPY shipping-method shipping-method
COPY events events
COPY order order
COPY frontoffice frontoffice
COPY backoffice backoffice
COPY frontrest frontrest

# Build avec retry
RUN mvn clean install -DskipTests -o || \
    mvn clean install -DskipTests || \
    (sleep 10 && mvn clean install -DskipTests)

# Image de production
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copier les JARs construits
COPY --from=build /app/frontoffice/target/*.jar frontoffice.jar
COPY --from=build /app/backoffice/target/*.jar backoffice.jar
COPY --from=build /app/frontrest/target/*.jar frontrest.jar

# Copier le script de démarrage
COPY docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8080 8081 8082

ENTRYPOINT ["/app/docker-entrypoint.sh"]