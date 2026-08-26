#!/usr/bin/env bash
# Run as root on a Linux host to install systemd unit and environment file for SptrngBoot
# Usage: sudo ./install-systemd.sh /opt/sptrngboot /usr/bin/java /path/to/sptrngboot-0.0.1-SNAPSHOT.jar

WORKDIR=${1:-/opt/sptrngboot}
JAVA_BIN=${2:-/usr/bin/java}
JAR_PATH=${3:-/opt/sptrngboot/sptrngboot-0.0.1-SNAPSHOT.jar}
ENV_FILE=/etc/sptrngboot/env
SERVICE_FILE=/etc/systemd/system/sptrngboot.service

mkdir -p "$WORKDIR"
cat > "$ENV_FILE" <<EOF
DB_URL=jdbc:mysql://localhost:3306/sptrngboot?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=sptrngboot
DB_PASSWORD=change-me
SPRING_PROFILES_ACTIVE=mysql
EOF
chmod 600 "$ENV_FILE"

cat > "$SERVICE_FILE" <<EOF
[Unit]
Description=SptrngBoot Spring Boot application
After=network.target

[Service]
User=root
WorkingDirectory=$WORKDIR
ExecStart=$JAVA_BIN -jar $JAR_PATH --spring.profiles.active=mysql
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
EnvironmentFile=$ENV_FILE

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable sptrngboot.service
systemctl start sptrngboot.service
systemctl status sptrngboot.service --no-pager
