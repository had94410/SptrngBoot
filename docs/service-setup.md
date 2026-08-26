# Service setup

This document shows how to run the SptrngBoot application as a service on Windows and Linux (systemd).

## Windows (PowerShell)

A pair of helper scripts were added under `scripts/`:

- `install-windows-service.ps1` - creates and starts a Windows service that runs the app
- `uninstall-windows-service.ps1` - stops and removes the service and the wrapper batch

Usage (run PowerShell as Administrator):

1. Build the project: `mvn clean package -DskipTests`
2. Open an elevated PowerShell in the project `scripts` folder and run:
   `.\
install-windows-service.ps1 -ServiceName sptrngboot -JarPath ..\target\sptrngboot-0.0.1-SNAPSHOT.jar -JavaPath 'C:\\Program Files\\Java\\jdk-17\\bin\\java.exe' -Profile mysql -Port 8080`
3. The script creates a wrapper batch `run-sptrngboot.bat` in the scripts folder. Edit it to add DB credentials or other environment variables if necessary.
4. To remove the service: `.\nuninstall-windows-service.ps1 -ServiceName sptrngboot`

Notes:
- The scripts must be run as Administrator.
- The wrapper batch runs the jar via `java -jar`. You can change Java options or set environment variables inside the wrapper.

## Linux (systemd)

Create a unit file at `/etc/systemd/system/sptrngboot.service` with the following contents:

```
[Unit]
Description=SptrngBoot Spring Boot application
After=network.target

[Service]
User=sptrng
WorkingDirectory=/opt/sptrngboot
ExecStart=/usr/bin/java -jar /opt/sptrngboot/sptrngboot-0.0.1-SNAPSHOT.jar --spring.profiles.active=mysql
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
Environment=DB_URL=jdbc:mysql://localhost:3306/sptrngboot?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
Environment=DB_USERNAME=sptrngboot
Environment=DB_PASSWORD=change-me

[Install]
WantedBy=multi-user.target
```

Commands:

```
sudo systemctl daemon-reload
sudo systemctl enable sptrngboot.service
sudo systemctl start sptrngboot.service
sudo systemctl status sptrngboot.service
```

Notes:
- Create a dedicated system user (`sptrng`) and place the jar in `/opt/sptrngboot`.
- Use a secret manager or systemd `EnvironmentFile=` for production credentials instead of putting passwords directly in the unit file.

## Credentials and secrets

- For Windows, prefer setting environment variables at the machine level (System Properties → Environment Variables) or using a Windows Secret Store. The wrapper batch can also source a `.env`-style file if you choose to implement that.
- For Linux, prefer systemd `EnvironmentFile=/etc/sptrngboot/env` (secure file with 600 perms) or a secret manager.

If you want, the scripts can be adjusted to register a different service user, create the WorkingDirectory for the service, or wire in environment files. Tell me which option you prefer and I can update the scripts accordingly.
