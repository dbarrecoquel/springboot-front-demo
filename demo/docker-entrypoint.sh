#!/bin/bash

set -e

echo "======================================"
echo "🚀 E-Commerce Application Starting"
echo "======================================"

# Fonction pour vérifier si PostgreSQL est prêt
wait_for_postgres() {
    echo "⏳ Waiting for PostgreSQL to be ready..."
    max_attempts=30
    attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if timeout 1 bash -c "cat < /dev/null > /dev/tcp/postgres/5432" 2>/dev/null; then
            echo "✅ PostgreSQL is ready!"
            return 0
        fi
        echo "   PostgreSQL is unavailable - sleeping (attempt $((attempt+1))/$max_attempts)"
        sleep 2
        attempt=$((attempt+1))
    done

    echo "❌ PostgreSQL did not become ready in time"
    exit 1
}

# Fonction pour vérifier si Kafka est prêt
wait_for_kafka() {
    echo "⏳ Waiting for Kafka to be ready..."
    max_attempts=30
    attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if timeout 1 bash -c "cat < /dev/null > /dev/tcp/kafka/9092" 2>/dev/null; then
            echo "✅ Kafka is ready!"
            return 0
        fi
        echo "   Kafka is unavailable - sleeping (attempt $((attempt+1))/$max_attempts)"
        sleep 2
        attempt=$((attempt+1))
    done

    echo "❌ Kafka did not become ready in time"
    exit 1
}

# Attendre PostgreSQL
wait_for_postgres

# Attendre Kafka
wait_for_kafka

# Attendre encore un peu
sleep 10

# Démarrer le BackOffice en arrière-plan
echo ""
echo "======================================"
echo "🔧 Starting BackOffice on port 8081"
echo "======================================"
java -jar backoffice.jar \
    --server.port=8081 \
    --spring.datasource.url=${SPRING_DATASOURCE_URL} \
    --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
    --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD} &

BACKOFFICE_PID=$!
echo "✅ BackOffice started with PID: $BACKOFFICE_PID"

# Attendre que BackOffice démarre
sleep 10

# Démarrer le FrontOffice en arrière-plan
echo ""
echo "======================================"
echo "🛒 Starting FrontOffice on port 8080"
echo "======================================"
java -jar frontoffice.jar \
    --server.port=8080 \
    --spring.datasource.url=${SPRING_DATASOURCE_URL} \
    --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
    --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD} &

FRONTOFFICE_PID=$!
echo "✅ FrontOffice started with PID: $FRONTOFFICE_PID"

# Attendre que FrontOffice démarre
sleep 10

# Démarrer le FrontRest avec DEBUG activé
echo ""
echo "======================================"
echo "🌐 Starting FrontRest API on port 8082 (DEBUG MODE)"
echo "======================================"
java -jar frontrest.jar \
    --server.port=8082 \
    --spring.datasource.url=${SPRING_DATASOURCE_URL} \
    --spring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
    --spring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
    --spring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP_SERVERS} \
    --debug &

FRONTREST_PID=$!
echo "✅ FrontRest started with PID: $FRONTREST_PID"

echo ""
echo "======================================"
echo "✅ All services started!"
echo "======================================"

# Fonction de nettoyage
cleanup() {
    echo ""
    echo "======================================"
    echo "🛑 Shutting down applications..."
    echo "======================================"
    kill $FRONTOFFICE_PID $BACKOFFICE_PID $FRONTREST_PID 2>/dev/null
    wait $FRONTOFFICE_PID $BACKOFFICE_PID $FRONTREST_PID 2>/dev/null
    echo "✅ Shutdown complete"
    exit 0
}

trap cleanup SIGTERM SIGINT

# Attendre que les processus se terminent
wait $FRONTOFFICE_PID $BACKOFFICE_PID $FRONTREST_PID