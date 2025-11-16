# Wateralmanak Project

Full-stack application with
- PostgreSQL/PostGIS
- Liquibase
- Java API (JAX-RS/Jersey)
- Keycloak
- Angular 20
- Nginx

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
```
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
