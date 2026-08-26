# Production deployment guide

## 1. Production & local MySQL setup

This project includes a dedicated MySQL profile for production and local development. For convenience there is also an `application-mysql.properties` profile you can use locally. To run the app with MySQL, create the database and user on your machine and then start the JVM with `SPRING_PROFILES_ACTIVE=mysql` (or `production` for the production profile).

Example environment variables (use `.env` / export as appropriate):

```bash
DB_URL='jdbc:mysql://localhost:3306/sptrngboot?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
DB_USERNAME='sptrngboot'
DB_PASSWORD='strong-db-password'
DB_DRIVER='com.mysql.cj.jdbc.Driver'
DB_DIALECT='org.hibernate.dialect.MySQLDialect'
```

Create the database and a user locally (MySQL):

```sql
CREATE DATABASE sptrngboot CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'sptrngboot'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON sptrngboot.* TO 'sptrngboot'@'localhost';
FLUSH PRIVILEGES;
```

Start locally with the MySQL profile:

```bash
# With Maven
SPRING_PROFILES_ACTIVE=mysql mvn spring-boot:run

# Or with the packaged jar
SPRING_PROFILES_ACTIVE=mysql java -jar target/sptrngboot-0.0.1-SNAPSHOT.jar
```

The project also includes a `application-production.properties` profile for typical production settings.

## 2. Admin login configuration

Set secure admin credentials before deployment:

```bash
export ADMIN_USERNAME='admin'
export ADMIN_PASSWORD='strong-production-password'
```

The app stores the admin user in the database and hashes the password using BCrypt. If no admin user exists yet, one is created automatically.

## 3. Brand asset replacement

Production branding files should live under the static resource folder used by the host:

- `/images/brand/logo.svg`
- `/images/brand/hero-banner.svg`
- `/images/placeholders/` for fallback or temporary content only

Keep the filenames stable so the templates continue to reference the same paths after the swap.

## 4. Final production checklist

- Confirm the MySQL database is reachable from the target host
- Set `ADMIN_USERNAME` and `ADMIN_PASSWORD` to production values
- Verify `/` loads correctly
- Verify `/login` and `/admin` are accessible after authentication
- Confirm the logo and hero banner are visible in the browser
- Check the site with a real browser after deployment, not just startup logs
- Confirm reverse proxy / SSL / firewall rules are configured on the hosting layer

## 5. Recommended deployment commands

```bash
mvn clean package -DskipTests
export SPRING_PROFILES_ACTIVE=production
java -jar target/sptrngboot-0.0.1-SNAPSHOT.jar
```

If the application is running behind a reverse proxy, ensure the proxy forwards the original host and protocol headers correctly.
