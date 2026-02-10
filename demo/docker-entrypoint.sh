#!/bin/bash

echo "🚀 Starting E-Commerce Application..."

# Démarrer le BackOffice en arrière-plan
echo "🔧 Starting BackOffice on port 8081..."
java -jar backoffice.jar --server.port=8081 &
BACKOFFICE_PID=$!

# Attendre un peu
sleep 5

# Démarrer le FrontOffice en premier plan
echo "🛒 Starting FrontOffice on port 8080..."
java -jar frontoffice.jar --server.port=8080 &
FRONTOFFICE_PID=$!

# Fonction de nettoyage
cleanup() {
    echo "🛑 Shutting down applications..."
    kill $FRONTOFFICE_PID $BACKOFFICE_PID
    wait $FRONTOFFICE_PID $BACKOFFICE_PID
    exit 0
}

trap cleanup SIGTERM SIGINT

# Attendre que les processus se terminent
wait $FRONTOFFICE_PID $BACKOFFICE_PID
