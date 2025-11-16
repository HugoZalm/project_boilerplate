## Project Structure

```
wateralmanak/
├── docker-compose.yml
├── .env
├── README.md
├── wait-for-it.sh
├── database/
│   └── init.sql
├── liquibase/
│   ├── Dockerfile
│   ├── changelog/
│   │   ├── db.changelog-master.xml
│   │   ├── changes/
│   │   │   ├── 001-create-schema.xml
│   │   │   └── 002-create-voorzieningen-table.xml
│   └── liquibase.properties
├── api/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── nl/
│           │       └── wateralmanak/
│           │           ├── config/
│           │           │   ├── ApplicationConfig.java
│           │           │   ├── CorsFilter.java
│           │           │   ├── DatabaseConfig.java
│           │           │   └── KeycloakSecurityFilter.java
│           │           ├── model/
│           │           │   └── Voorziening.java
│           │           ├── repository/
│           │           │   └── VoorzieningRepository.java
│           │           ├── service/
│           │           │   └── VoorzieningService.java
│           │           └── resource/
│           │               ├── VoorzieningResource.java
│           │               └── HealthResource.java
│           ├── resources/
│           │   ├── application.properties
│           │   └── META-INF/
│           │       └── beans.xml
│           └── webapp/
│               └── WEB-INF/
│                   └── web.xml
├── keycloak/
│   └── realm-config/
│       └── wateralmanak-realm.json
├── frontend/
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── package.json
│   ├── angular.json
│   ├── tsconfig.json
│   ├── tsconfig.app.json
│   └── src/
│       ├── main.ts
│       ├── index.html
│       ├── styles.css
│       ├── app/
│       │   ├── app.config.ts
│       │   ├── app.routes.ts
│       │   ├── app.component.ts
│       │   ├── app.component.html
│       │   ├── app.component.css
│       │   ├── services/
│       │   │   ├── auth.service.ts
│       │   │   └── voorziening.service.ts
│       │   ├── interceptors/
│       │   │   └── auth.interceptor.ts
│       │   ├── guards/
│       │   │   └── auth.guard.ts
│       │   ├── models/
│       │   │   └── voorziening.model.ts
│       │   └── components/
│       │       ├── home/
│       │       │   ├── home.component.ts
│       │       │   ├── home.component.html
│       │       │   └── home.component.css
│       │       └── voorzieningen/
│       │           ├── voorzieningen.component.ts
│       │           ├── voorzieningen.component.html
│       │           └── voorzieningen.component.css
│       └── environments/
│           └── environment.ts
├── nginx/
│   ├── Dockerfile
│   └── nginx.conf
└── postman/
    ├── Wateralmanak.postman_collection.json
    └── Wateralmanak.postman_environment.json
```

---

## File Contents

### `/docker-compose.yml`

```yaml
version: '3.8'

services:
  postgres:
    image: postgis/postgis:15-3.3
    container_name: wateralmanak-postgres
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - wateralmanak-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5

  liquibase:
    build: ./liquibase
    container_name: wateralmanak-liquibase
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      LIQUIBASE_COMMAND_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      LIQUIBASE_COMMAND_USERNAME: ${POSTGRES_USER}
      LIQUIBASE_COMMAND_PASSWORD: ${POSTGRES_PASSWORD}
    networks:
      - wateralmanak-network

  keycloak:
    image: quay.io/keycloak/keycloak:23.0
    container_name: wateralmanak-keycloak
    command: start-dev --import-realm
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/${KEYCLOAK_DB}
      KC_DB_USERNAME: ${POSTGRES_USER}
      KC_DB_PASSWORD: ${POSTGRES_PASSWORD}
      KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD}
      KC_HOSTNAME_STRICT: false
      KC_HOSTNAME_STRICT_HTTPS: false
      KC_HTTP_ENABLED: true
      KC_PROXY: edge
    ports:
      - "8080:8080"
    volumes:
      - ./keycloak/realm-config:/opt/keycloak/data/import
    networks:
      - wateralmanak-network
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "exec 3<>/dev/tcp/127.0.0.1/8080;echo -e 'GET /health/ready HTTP/1.1\r\nhost: http://localhost\r\nConnection: close\r\n\r\n' >&3;if [ $? -eq 0 ]; then echo 'Healthcheck Successful';exit 0;else echo 'Healthcheck Failed';exit 1;fi;"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 90s

  api:
    build: ./api
    container_name: wateralmanak-api
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      DB_USER: ${POSTGRES_USER}
      DB_PASSWORD: ${POSTGRES_PASSWORD}
      KEYCLOAK_URL: http://keycloak:8080
      KEYCLOAK_REALM: wateralmanak
    ports:
      - "8081:8080"
    networks:
      - wateralmanak-network
    depends_on:
      postgres:
        condition: service_healthy
      keycloak:
        condition: service_healthy
      liquibase:
        condition: service_completed_successfully
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8080/api/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  frontend:
    build: ./frontend
    container_name: wateralmanak-frontend
    networks:
      - wateralmanak-network
    depends_on:
      - api
      - keycloak

  nginx:
    build: ./nginx
    container_name: wateralmanak-nginx
    ports:
      - "80:80"
    networks:
      - wateralmanak-network
    depends_on:
      - frontend
      - api
      - keycloak
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres_data:
    driver: local

networks:
  wateralmanak-network:
    driver: bridge
```

---

### `/.env`

```env
# PostgreSQL
POSTGRES_DB=wateralmanak
POSTGRES_USER=wateralmanak_user
POSTGRES_PASSWORD=wateralmanak_pass123

# Keycloak Database
KEYCLOAK_DB=keycloak

# Keycloak Admin
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin123
```

---

### `/README.md`

```markdown
# Wateralmanak Project

Full-stack application with PostgreSQL/PostGIS, Liquibase, Java API (JAX-RS/Jersey), Keycloak, Angular 20, and Nginx.

## Prerequisites

- Docker & Docker Compose
- Postman (for API testing)

## Quick Start

1. Clone the repository
2. Navigate to project root
3. Start all services:

```bash
docker-compose up -d
```

4. Wait for all services to be healthy (2-3 minutes):

```bash
docker-compose ps
```

## Service URLs

- **Frontend**: http://localhost
- **API**: http://localhost/api
- **Keycloak**: http://localhost/auth
- **Keycloak Admin**: http://localhost/auth/admin (admin/admin123)

## Initial Setup

### Keycloak Configuration

The realm is auto-imported. Default users:

- **Admin User**: admin@wateralmanak.nl / admin123 (admin role)
- **Regular User**: user@wateralmanak.nl / user123 (user role)

### Testing with Postman

1. Import `postman/Wateralmanak.postman_collection.json`
2. Import `postman/Wateralmanak.postman_environment.json`
3. Run requests in order

## Development

### Rebuild Services

```bash
# Rebuild all
docker-compose up -d --build

# Rebuild specific service
docker-compose up -d --build api
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f api
```

### Stop Services

```bash
docker-compose down

# Remove volumes (delete data)
docker-compose down -v
```

## Database Access

```bash
docker exec -it wateralmanak-postgres psql -U wateralmanak_user -d wateralmanak
```

## Architecture

- **PostgreSQL/PostGIS**: Data persistence with spatial support
- **Liquibase**: Database version control
- **Java API**: JAX-RS/Jersey REST services with Keycloak JWT validation
- **Keycloak**: Identity and access management
- **Angular 20**: Modern SPA with Keycloak integration
- **Nginx**: Reverse proxy for all services

## API Endpoints

- `GET /api/voorzieningen` - List all (requires user role)
- `GET /api/voorzieningen/{id}` - Get by ID (requires user role)
- `POST /api/voorzieningen` - Create (requires admin role)
- `PUT /api/voorzieningen/{id}` - Update (requires admin role)
- `DELETE /api/voorzieningen/{id}` - Delete (requires admin role)
- `GET /api/health` - Health check (public)

## Security

- All API endpoints (except health) require JWT tokens
- CORS configured for nginx proxy
- Roles: `admin`, `user`
- Groups: `admins`, `users`
```

---

### `/wait-for-it.sh`

```bash
#!/usr/bin/env bash
# Use this script to test if a given TCP host/port are available

set -e

WAITFORIT_cmdname=${0##*/}

echoerr() { if [[ $WAITFORIT_QUIET -ne 1 ]]; then echo "$@" 1>&2; fi }

usage()
{
    cat << USAGE >&2
Usage:
    $WAITFORIT_cmdname host:port [-s] [-t timeout] [-- command args]
    -h HOST | --host=HOST       Host or IP under test
    -p PORT | --port=PORT       TCP port under test
                                Alternatively, you specify the host and port as host:port
    -s | --strict               Only execute subcommand if the test succeeds
    -q | --quiet                Don't output any status messages
    -t TIMEOUT | --timeout=TIMEOUT
                                Timeout in seconds, zero for no timeout
    -- COMMAND ARGS             Execute command with args after the test finishes
USAGE
    exit 1
}

wait_for()
{
    if [[ $WAITFORIT_TIMEOUT -gt 0 ]]; then
        echoerr "$WAITFORIT_cmdname: waiting $WAITFORIT_TIMEOUT seconds for $WAITFORIT_HOST:$WAITFORIT_PORT"
    else
        echoerr "$WAITFORIT_cmdname: waiting for $WAITFORIT_HOST:$WAITFORIT_PORT without a timeout"
    fi
    WAITFORIT_start_ts=$(date +%s)
    while :
    do
        if [[ $WAITFORIT_ISBUSY -eq 1 ]]; then
            nc -z $WAITFORIT_HOST $WAITFORIT_PORT
            WAITFORIT_result=$?
        else
            (echo -n > /dev/tcp/$WAITFORIT_HOST/$WAITFORIT_PORT) >/dev/null 2>&1
            WAITFORIT_result=$?
        fi
        if [[ $WAITFORIT_result -eq 0 ]]; then
            WAITFORIT_end_ts=$(date +%s)
            echoerr "$WAITFORIT_cmdname: $WAITFORIT_HOST:$WAITFORIT_PORT is available after $((WAITFORIT_end_ts - WAITFORIT_start_ts)) seconds"
            break
        fi
        sleep 1
    done
    return $WAITFORIT_result
}

wait_for_wrapper()
{
    # In order to support SIGINT during timeout: http://unix.stackexchange.com/a/169254
    if [[ $WAITFORIT_QUIET -eq 1 ]]; then
        timeout $WAITFORIT_BUSYTIMEFLAG $WAITFORIT_TIMEOUT $0 --quiet --child --host=$WAITFORIT_HOST --port=$WAITFORIT_PORT --timeout=$WAITFORIT_TIMEOUT &
    else
        timeout $WAITFORIT_BUSYTIMEFLAG $WAITFORIT_TIMEOUT $0 --child --host=$WAITFORIT_HOST --port=$WAITFORIT_PORT --timeout=$WAITFORIT_TIMEOUT &
    fi
    WAITFORIT_PID=$!
    trap "kill -INT -$WAITFORIT_PID" INT
    wait $WAITFORIT_PID
    WAITFORIT_RESULT=$?
    if [[ $WAITFORIT_RESULT -ne 0 ]]; then
        echoerr "$WAITFORIT_cmdname: timeout occurred after waiting $WAITFORIT_TIMEOUT seconds for $WAITFORIT_HOST:$WAITFORIT_PORT"
    fi
    return $WAITFORIT_RESULT
}

# process arguments
while [[ $# -gt 0 ]]
do
    case "$1" in
        *:* )
        WAITFORIT_hostport=(${1//:/ })
        WAITFORIT_HOST=${WAITFORIT_hostport[0]}
        WAITFORIT_PORT=${WAITFORIT_hostport[1]}
        shift 1
        ;;
        --child)
        WAITFORIT_CHILD=1
        shift 1
        ;;
        -q | --quiet)
        WAITFORIT_QUIET=1
        shift 1
        ;;
        -s | --strict)
        WAITFORIT_STRICT=1
        shift 1
        ;;
        -h)
        WAITFORIT_HOST="$2"
        if [[ $WAITFORIT_HOST == "" ]]; then break; fi
        shift 2
        ;;
        --host=*)
        WAITFORIT_HOST="${1#*=}"
        shift 1
        ;;
        -p)
        WAITFORIT_PORT="$2"
        if [[ $WAITFORIT_PORT == "" ]]; then break; fi
        shift 2
        ;;
        --port=*)
        WAITFORIT_PORT="${1#*=}"
        shift 1
        ;;
        -t)
        WAITFORIT_TIMEOUT="$2"
        if [[ $WAITFORIT_TIMEOUT == "" ]]; then break; fi
        shift 2
        ;;
        --timeout=*)
        WAITFORIT_TIMEOUT="${1#*=}"
        shift 1
        ;;
        --)
        shift
        WAITFORIT_CLI=("$@")
        break
        ;;
        --help)
        usage
        ;;
        *)
        echoerr "Unknown argument: $1"
        usage
        ;;
    esac
done

if [[ "$WAITFORIT_HOST" == "" || "$WAITFORIT_PORT" == "" ]]; then
    echoerr "Error: you need to provide a host and port to test."
    usage
fi

WAITFORIT_TIMEOUT=${WAITFORIT_TIMEOUT:-15}
WAITFORIT_STRICT=${WAITFORIT_STRICT:-0}
WAITFORIT_CHILD=${WAITFORIT_CHILD:-0}
WAITFORIT_QUIET=${WAITFORIT_QUIET:-0}

# Check to see if timeout is from busybox?
WAITFORIT_TIMEOUT_PATH=$(type -p timeout)
WAITFORIT_TIMEOUT_PATH=$(realpath $WAITFORIT_TIMEOUT_PATH 2>/dev/null || readlink -f $WAITFORIT_TIMEOUT_PATH)

WAITFORIT_BUSYTIMEFLAG=""
if [[ $WAITFORIT_TIMEOUT_PATH =~ "busybox" ]]; then
    WAITFORIT_ISBUSY=1
    # Check if busybox timeout uses -t flag
    # (recent Alpine versions don't support -t anymore)
    if timeout &>/dev/stdout | grep -q -e '-t '; then
        WAITFORIT_BUSYTIMEFLAG="-t"
    fi
else
    WAITFORIT_ISBUSY=0
fi

if [[ $WAITFORIT_CHILD -gt 0 ]]; then
    wait_for
    WAITFORIT_RESULT=$?
    exit $WAITFORIT_RESULT
else
    if [[ $WAITFORIT_TIMEOUT -gt 0 ]]; then
        wait_for_wrapper
        WAITFORIT_RESULT=$?
    else
        wait_for
        WAITFORIT_RESULT=$?
    fi
fi

if [[ $WAITFORIT_CLI != "" ]]; then
    if [[ $WAITFORIT_RESULT -ne 0 && $WAITFORIT_STRICT -eq 1 ]]; then
        echoerr "$WAITFORIT_cmdname: strict mode, refusing to execute subprocess"
        exit $WAITFORIT_RESULT
    fi
    exec "${WAITFORIT_CLI[@]}"
else
    exit $WAITFORIT_RESULT
fi
```

---

### `/database/init.sql`

```sql
-- Initialize database
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create keycloak database if it doesn't exist
SELECT 'CREATE DATABASE keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec
```

---

### `/liquibase/Dockerfile`

```dockerfile
FROM liquibase/liquibase:4.25

USER root

COPY changelog /liquibase/changelog
COPY liquibase.properties /liquibase/liquibase.properties

RUN chmod -R 755 /liquibase

USER liquibase

CMD ["--defaults-file=/liquibase/liquibase.properties", "update"]
```

---

### `/liquibase/liquibase.properties`

```properties
changeLogFile=changelog/db.changelog-master.xml
liquibase.command.url=${LIQUIBASE_COMMAND_URL}
liquibase.command.username=${LIQUIBASE_COMMAND_USERNAME}
liquibase.command.password=${LIQUIBASE_COMMAND_PASSWORD}
```

---

### `/liquibase/changelog/db.changelog-master.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.25.xsd">

    <include file="changes/001-create-schema.xml" relativeToChangelogFile="true"/>
    <include file="changes/002-create-voorzieningen-table.xml" relativeToChangelogFile="true"/>

</databaseChangeLog>
```

---

### `/liquibase/changelog/changes/001-create-schema.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.25.xsd">

    <changeSet id="001-create-schema" author="wateralmanak">
        <sql>
            CREATE SCHEMA IF NOT EXISTS wateralmanak;
        </sql>
        <rollback>
            DROP SCHEMA IF EXISTS wateralmanak CASCADE;
        </rollback>
    </changeSet>

</databaseChangeLog>
```

---

### `/liquibase/changelog/changes/002-create-voorzieningen-table.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.25.xsd">

    <changeSet id="002-create-voorzieningen-table" author="wateralmanak">
        <sql>
            CREATE TABLE wateralmanak.voorzieningen (
                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                naam VARCHAR(255) NOT NULL,
                beschrijving TEXT,
                locatie GEOMETRY(Point, 4326),
                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
            );

            CREATE INDEX idx_voorzieningen_locatie ON wateralmanak.voorzieningen USING GIST(locatie);
            CREATE INDEX idx_voorzieningen_naam ON wateralmanak.voorzieningen(naam);

            -- Insert sample data
            INSERT INTO wateralmanak.voorzieningen (naam, beschrijving, locatie) VALUES
            ('Waterpomp Rotterdam', 'Hoofdwaterpomp centrum', ST_SetSRID(ST_MakePoint(4.4777, 51.9225), 4326)),
            ('Sluis Kinderdijk', 'Historische sluis', ST_SetSRID(ST_MakePoint(4.6395, 51.8833), 4326)),
            ('Waterzuivering Delft', 'Moderne zuiveringsinstallatie', ST_SetSRID(ST_MakePoint(4.3571, 52.0116), 4326));
        </sql>
        <rollback>
            DROP TABLE IF EXISTS wateralmanak.voorzieningen;
        </rollback>
    </changeSet>

</databaseChangeLog>
```

---
