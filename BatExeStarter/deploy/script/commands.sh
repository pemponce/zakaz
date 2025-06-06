#!/bin/bash


PASSWORD="9200@uYv!nQYdL"

execute_with_sudo() {
    echo "$PASSWORD" | sudo -S "$@"
}

ls


echo "$PASSWORD" | sudo -S mv ~/docker-compose.yml /home/bot/

cd ../bot
echo "$PASSWORD" | sudo -S mkdir -p image
echo "$PASSWORD" | sudo -S mv ~/Dockerfile /home/bot/image/

cd image
echo "$PASSWORD" | sudo -S mkdir -p default
echo "$PASSWORD" | sudo -S mv ~/zakaz-0.0.1-SNAPSHOT.jar /home/bot/image/default
echo "$PASSWORD" | sudo -S mv ~/run.sh /home/bot/image/default

cd ../

echo "$PASSWORD" | sudo -S apt update

docker compose up -d
docker attach zakaz
