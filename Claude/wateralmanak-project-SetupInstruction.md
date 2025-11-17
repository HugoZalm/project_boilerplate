## Setup Instructions

### 1. Initial Setup

```bash
# Clone or create the project structure
mkdir wateralmanak && cd wateralmanak

# Make wait-for-it.sh executable (use git-bash terminal)
chmod +x wait-for-it.sh

# Start all services
docker-compose up -d

# Wait for services to be healthy (2-3 minutes)
docker-compose ps
```

### 2. Verify Services

```bash
# Check all containers are running
docker-compose ps

# View logs if needed
docker-compose logs -f keycloak
docker-compose logs -f api
docker-compose logs -f frontend
```

### 3. Access Applications

- **Frontend**: http://localhost
- **API Health**: http://localhost/api/health
- **Keycloak Admin**: http://localhost/auth/admin
  - Username: `admin`
  - Password: `admin123`

### 4. Test with Postman

1. Import both JSON files into Postman
2. Select "Wateralmanak Environment"
3. Run requests in this order:
   - **Authentication** > "Login Admin User"
   - **Voorzieningen** > "Get All Voorzieningen"
   - **Voorzieningen** > "Create Voorziening" (admin only)
   - **Voorzieningen** > "Update Voorziening" (admin only)
   - **Voorzieningen** > "Delete Voorziening" (admin only)

### 5. Create Additional Users

1. Run "Get Admin Token" in Postman
2. Run "Create New User" request
3. Modify the JSON body as needed

---

## Project Complete!

This project includes:
- ✅ Docker Compose orchestration
- ✅ PostgreSQL/PostGIS database with persistent volumes
- ✅ Liquibase database migrations
- ✅ Java API with JAX-RS/Jersey (OpenJDK 21, Maven)
- ✅ Keycloak authentication with JWT tokens
- ✅ Role-based access control (admin/user roles)
- ✅ Angular 20 frontend with Keycloak integration
- ✅ Nginx reverse proxy
- ✅ Full CRUD operations for Voorzieningen
- ✅ PostGIS spatial data support
- ✅ CORS configuration
- ✅ Complete Postman collection

---

## Troubleshooting

### Services Won't Start

```bash
# Check logs for specific service
docker-compose logs keycloak
docker-compose logs postgres
docker-compose logs api

# Restart a specific service
docker-compose restart api

# Rebuild and restart
docker-compose up -d --build api
```

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Connect to database manually
docker exec -it wateralmanak-postgres psql -U wateralmanak_user -d wateralmanak

# Run SQL query to check tables
\dt wateralmanak.*
```

### Keycloak Issues

```bash
# Check Keycloak logs
docker-compose logs -f keycloak

# Restart Keycloak
docker-compose restart keycloak

# Verify realm import
# Login to http://localhost/auth/admin
# Check if 'wateralmanak' realm exists
```

### API Not Responding

```bash
# Check API logs
docker-compose logs -f api

# Test health endpoint
curl http://localhost/api/health

# Rebuild API
docker-compose up -d --build api
```

### Frontend Issues

```bash
# Check frontend logs
docker-compose logs -f frontend

# Rebuild frontend
docker-compose up -d --build frontend

# Check nginx logs
docker-compose logs -f nginx
```

### Port Conflicts

If ports 80, 5432, 8080, or 8081 are already in use:

Edit `docker-compose.yml` and change the port mappings:
```yaml
ports:
  - "8888:80"  # Change 80 to 8888 for nginx
```

Then update the Postman environment and Angular environment files accordingly.

---

## Testing Guide

### 1. Test Database Connection

```bash
# Connect to PostgreSQL
docker exec -it wateralmanak-postgres psql -U wateralmanak_user -d wateralmanak

# List tables
\dt wateralmanak.*

# Query sample data
SELECT id, naam, ST_AsText(locatie) as location FROM wateralmanak.voorzieningen;

# Exit
\q
```

### 2. Test API Endpoints Manually

```bash
# Health check (no auth required)
curl http://localhost/api/health

# Get all voorzieningen (requires auth - will fail without token)
curl http://localhost/api/voorzieningen
# Expected: 401 Unauthorized

# Login and get token (use Postman for easier token management)
```

### 3. Test Frontend

1. Open browser: http://localhost
2. Click "Login with Keycloak"
3. Login with:
   - Admin: admin@wateralmanak.nl / admin123
   - User: user@wateralmanak.nl / user123
4. Navigate to "Voorzieningen"
5. Test CRUD operations (admin can create/edit/delete, user can only view)

### 4. Test Keycloak

1. Access Keycloak Admin: http://localhost/auth/admin
2. Login with admin/admin123
3. Select "wateralmanak" realm
4. Check:
   - Users exist
   - Roles are configured
   - Groups are set up
   - Clients are configured

---

## Advanced Configuration

### Custom Environment Variables

Create a `.env.local` file (add to .gitignore):

```env
# Override default values
POSTGRES_PASSWORD=my_secure_password
KEYCLOAK_ADMIN_PASSWORD=my_admin_password
```

Use it:
```bash
docker-compose --env-file .env.local up -d
```

### Enable HTTPS (Production)

1. Update nginx configuration:
```nginx
server {
    listen 443 ssl http2;
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    # ... rest of config
}
```

2. Update Keycloak for HTTPS:
```yaml
environment:
  KC_HOSTNAME_STRICT_HTTPS: true
```

### Backup Database

```bash
# Create backup
docker exec wateralmanak-postgres pg_dump -U wateralmanak_user wateralmanak > backup.sql

# Restore backup
cat backup.sql | docker exec -i wateralmanak-postgres psql -U wateralmanak_user -d wateralmanak
```

### Scale Services

```bash
# Scale API to 3 instances
docker-compose up -d --scale api=3

# Update nginx upstream for load balancing
```

---

## API Reference

### Authentication

All endpoints except `/api/health` require a Bearer token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

### Endpoints

#### Health Check
```
GET /api/health
```
Response:
```json
{
  "status": "UP",
  "database": "connected"
}
```

#### List All Voorzieningen
```
GET /api/voorzieningen
```
Requires: `user` or `admin` role

Response:
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "naam": "Waterpomp Rotterdam",
    "beschrijving": "Hoofdwaterpomp centrum",
    "longitude": 4.4777,
    "latitude": 51.9225,
    "createdAt": "2024-11-16T10:00:00.000Z",
    "updatedAt": "2024-11-16T10:00:00.000Z"
  }
]
```

#### Get Voorziening by ID
```
GET /api/voorzieningen/{id}
```
Requires: `user` or `admin` role

Response: Same as single voorziening object above

#### Create Voorziening
```
POST /api/voorzieningen
```
Requires: `admin` role

Request Body:
```json
{
  "naam": "New Waterpomp",
  "beschrijving": "Description here",
  "longitude": 4.5000,
  "latitude": 51.9000
}
```

Response: 201 Created with voorziening object

#### Update Voorziening
```
PUT /api/voorzieningen/{id}
```
Requires: `admin` role

Request Body: Same as create

Response: 200 OK with updated voorziening object

#### Delete Voorziening
```
DELETE /api/voorzieningen/{id}
```
Requires: `admin` role

Response: 204 No Content

---

## Development Workflow

### Local Development (without Docker)

#### Backend (API)

```bash
cd api

# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/wateralmanak
export DB_USER=wateralmanak_user
export DB_PASSWORD=wateralmanak_pass123
export KEYCLOAK_URL=http://localhost:8080
export KEYCLOAK_REALM=wateralmanak

# Run with Maven
mvn clean install
mvn jetty:run

# API will be available at http://localhost:8080/api
```

#### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Update environment for local development
# Edit src/environments/environment.ts

# Run development server
npm start

# Frontend will be available at http://localhost:4200
```

### Making Changes

#### Add New Database Table

1. Create new Liquibase changelog in `liquibase/changelog/changes/`
2. Update `db.changelog-master.xml` to include it
3. Restart liquibase service:
```bash
docker-compose up -d liquibase
```

#### Add New API Endpoint

1. Create new Resource class in `api/src/main/java/nl/wateralmanak/resource/`
2. Register it in `ApplicationConfig.java`
3. Rebuild API:
```bash
docker-compose up -d --build api
```

#### Add New Angular Component

```bash
cd frontend

# Generate new component
ng generate component components/new-component

# Update routing if needed
# Edit app.routes.ts
```

#### Update Keycloak Configuration

1. Modify `keycloak/realm-config/wateralmanak-realm.json`
2. Remove keycloak container and volume:
```bash
docker-compose down
docker volume rm wateralmanak_postgres_data
docker-compose up -d
```

---

## Production Deployment Checklist

- [ ] Change all default passwords in `.env`
- [ ] Enable HTTPS/SSL certificates
- [ ] Configure Keycloak for production mode (disable dev mode)
- [ ] Set up database backups
- [ ] Configure proper logging (ELK stack, etc.)
- [ ] Set up monitoring (Prometheus, Grafana)
- [ ] Review and harden security settings
- [ ] Configure rate limiting
- [ ] Set up CI/CD pipeline
- [ ] Configure environment-specific properties
- [ ] Review CORS settings for production domains
- [ ] Set up secrets management (Vault, AWS Secrets Manager)
- [ ] Configure database connection pooling
- [ ] Set up health checks and readiness probes
- [ ] Configure proper memory limits for containers
- [ ] Set up log rotation
- [ ] Review and optimize Docker images
- [ ] Configure reverse proxy caching if needed
- [ ] Set up database replication/clustering
- [ ] Configure Keycloak clustering for high availability

---

## Performance Optimization

### Database

```sql
-- Add indexes for commonly queried fields
CREATE INDEX idx_voorzieningen_naam_gin ON wateralmanak.voorzieningen USING gin(to_tsvector('dutch', naam));

-- Analyze query performance
EXPLAIN ANALYZE SELECT * FROM wateralmanak.voorzieningen WHERE naam LIKE '%water%';
```

### API

- Configure HikariCP pool size based on load
- Enable Jersey request caching for read-heavy endpoints
- Implement pagination for large result sets
- Add response compression

### Frontend

- Enable Angular production mode
- Implement lazy loading for routes
- Add service worker for caching
- Optimize bundle size

### Nginx

```nginx
# Enable gzip compression
gzip on;
gzip_types text/plain text/css application/json application/javascript;

# Enable caching
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

---

## Security Best Practices

### API Security

1. **Never log sensitive data** (passwords, tokens)
2. **Validate all inputs** at API layer
3. **Use parameterized queries** (already implemented)
4. **Implement rate limiting** for public endpoints
5. **Keep dependencies updated** regularly
6. **Use security headers** in responses

### Keycloak Security

1. **Enable HTTPS** in production
2. **Configure strong password policies**
3. **Enable brute force protection** (already enabled)
4. **Set appropriate token lifespans**
5. **Use separate realms** for different environments
6. **Regularly audit user access**

### Database Security

1. **Use strong passwords**
2. **Limit database user permissions**
3. **Enable SSL for database connections** in production
4. **Regular security updates**
5. **Implement backup encryption**

### Frontend Security

1. **Never store sensitive data** in localStorage
2. **Implement CSP headers**
3. **Keep Angular and dependencies updated**
4. **Sanitize user inputs**
5. **Use HTTPS only** in production

---

## File Permissions Reminder

After creating the project, set proper permissions:

```bash
# Make scripts executable
chmod +x wait-for-it.sh

# Set proper ownership (if needed)
sudo chown -R $USER:$USER .

# Secure sensitive files
chmod 600 .env
```

---

## Quick Commands Reference

```bash
# Start everything
docker-compose up -d

# Stop everything
docker-compose down

# Stop and remove volumes (deletes all data!)
docker-compose down -v

# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f api

# Rebuild specific service
docker-compose up -d --build api

# Restart specific service
docker-compose restart keycloak

# Check service status
docker-compose ps

# Execute command in container
docker-compose exec postgres psql -U wateralmanak_user -d wateralmanak

# View resource usage
docker stats

# Clean up unused resources
docker system prune -a
```

---

## Support and Documentation

- **Docker**: https://docs.docker.com/
- **PostgreSQL**: https://www.postgresql.org/docs/
- **PostGIS**: https://postgis.net/documentation/
- **Liquibase**: https://docs.liquibase.com/
- **Jersey (JAX-RS)**: https://eclipse-ee4j.github.io/jersey/
- **Keycloak**: https://www.keycloak.org/documentation
- **Angular**: https://angular.io/docs
- **Nginx**: https://nginx.org/en/docs/

---

## License

This project structure is provided as-is for development purposes. Ensure you comply with all applicable licenses for the included technologies and frameworks.

---

**End of Documentation**

All files and configurations are complete and ready to use!# Wateralmanak Full-Stack Project
