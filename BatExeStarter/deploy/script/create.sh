#!/bin/bash

PASSWORD="a785410a"

# Обновление списка пакетов
echo "$PASSWORD" | sudo -S apt update
# Установка Java
echo "$PASSWORD" | sudo -S apt install -y default-jdk
# Установка необходимых пакетов для Docker
echo "$PASSWORD" | sudo -S apt install -y apt-transport-https ca-certificates curl gnupg2 software-properties-common
# Добавление ключа GPG Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
# Добавление репозитория Docker
echo "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list
# Обновление списка пакетов после добавления репозитория Docker
echo "$PASSWORD" | sudo -S apt update
# Установка Docker
echo "$PASSWORD" | sudo -S apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
# Проверка установки Docker
docker --version
sudo gpasswd -a $USER docker
echo "$PASSWORD" | sudo -S systemctl start docker
echo "$PASSWORD" | sudo -S apt install -y ufw
echo "$PASSWORD" | sudo -S ufw --force enable
sudo ufw default deny incoming
echo "$PASSWORD" | sudo -S ufw allow 22/tcp
echo "$PASSWORD" | sudo -S ufw allow 8888/tcp
echo "$PASSWORD" | sudo -S ufw allow 8888/udp

echo "$PASSWORD" | sudo -S ufw allow 8080/tcp
echo "$PASSWORD" | sudo -S ufw allow 8080/udp

echo "$PASSWORD" | sudo -S ufw allow 5432/tcp
echo "$PASSWORD" | sudo -S ufw allow 5432/udp

echo "$PASSWORD" | sudo -S ufw allow 3050/tcp
echo "$PASSWORD" | sudo -S ufw allow 3050/udp

echo "$PASSWORD" | sudo -S ufw status
# Создание директории bot
echo "$PASSWORD" | sudo -S mkdir -p /home/bot
echo directory creation babababa
cd /home/bot
