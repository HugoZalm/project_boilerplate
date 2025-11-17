### `/api/Dockerfile`

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/wateralmanak-api.war /app/wateralmanak-api.war

EXPOSE 8080

CMD ["java", "-jar", "/app/wateralmanak-api.war"]
```

---

### `/api/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>nl.wateralmanak</groupId>
    <artifactId>wateralmanak-api</artifactId>
    <version>1.0.0</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <jersey.version>3.1.5</jersey.version>
        <jakarta.version>6.0.0</jakarta.version>
    </properties>

    <dependencies>
        <!-- Jakarta EE -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>10.0.0</version>
            <scope>provided</scope>
        </dependency>

        <!-- Jersey (JAX-RS Implementation) -->
        <dependency>
            <groupId>org.glassfish.jersey.containers</groupId>
            <artifactId>jersey-container-servlet</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.inject</groupId>
            <artifactId>jersey-hk2</artifactId>
            <version>${jersey.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jersey.media</groupId>
            <artifactId>jersey-media-json-jackson</artifactId>
            <version>${jersey.version}</version>
        </dependency>

        <!-- PostgreSQL Driver -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.1</version>
        </dependency>

        <!-- PostGIS -->
        <dependency>
            <groupId>net.postgis</groupId>
            <artifactId>postgis-jdbc</artifactId>
            <version>2023.1.0</version>
        </dependency>

        <!-- HikariCP -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.1.0</version>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Jackson for JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.16.1</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.16.1</version>
        </dependency>

        <!-- SLF4J -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.9</version>
        </dependency>
    </dependencies>

    <build>
        <finalName>wateralmanak-api</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.12.1</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.eclipse.jetty</groupId>
                <artifactId>jetty-maven-plugin</artifactId>
                <version>11.0.19</version>
                <configuration>
                    <httpConnector>
                        <port>8080</port>
                    </httpConnector>
                    <webApp>
                        <contextPath>/</contextPath>
                    </webApp>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### `/api/src/main/java/nl/wateralmanak/config/ApplicationConfig.java`

```java
package nl.wateralmanak.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        // Resources
        classes.add(nl.wateralmanak.resource.VoorzieningResource.class);
        classes.add(nl.wateralmanak.resource.HealthResource.class);
        
        // Filters
        classes.add(CorsFilter.class);
        classes.add(KeycloakSecurityFilter.class);
        
        return classes;
    }
}
```

---

### `/api/src/main/java/nl/wateralmanak/config/CorsFilter.java`

```java
package nl.wateralmanak.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Headers",
                "origin, content-type, accept, authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
```

---

### `/api/src/main/java/nl/wateralmanak/config/DatabaseConfig.java`

```java
package nl.wateralmanak.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DatabaseConfig {
    
    private static HikariDataSource dataSource;
    
    static {
        try {
            Class.forName("org.postgresql.Driver");
            Class.forName("org.postgis.DriverWrapper");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL/PostGIS driver not found", e);
        }
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv().getOrDefault("DB_URL", 
            "jdbc:postgresql://postgres:5432/wateralmanak"));
        config.setUsername(System.getenv().getOrDefault("DB_USER", "wateralmanak_user"));
        config.setPassword(System.getenv().getOrDefault("DB_PASSWORD", "wateralmanak_pass123"));
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        dataSource = new HikariDataSource(config);
    }
    
    public static DataSource getDataSource() {
        return dataSource;
    }
    
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
```

---

### `/api/src/main/java/nl/wateralmanak/config/KeycloakSecurityFilter.java`

```java
package nl.wateralmanak.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class KeycloakSecurityFilter implements ContainerRequestFilter {

    private static final String KEYCLOAK_URL = System.getenv()
        .getOrDefault("KEYCLOAK_URL", "http://keycloak:8080");
    private static final String REALM = System.getenv()
        .getOrDefault("KEYCLOAK_REALM", "wateralmanak");
    private static PublicKey publicKey;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        
        // Skip authentication for health endpoint and OPTIONS requests
        if (path.equals("health") || requestContext.getMethod().equals("OPTIONS")) {
            return;
        }

        String authHeader = requestContext.getHeaderString("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abortWithUnauthorized(requestContext);
            return;
        }

        String token = authHeader.substring(7);
        
        try {
            Claims claims = validateToken(token);
            
            // Extract roles from token
            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            
            // Store roles in request context for endpoint authorization
            requestContext.setProperty("roles", roles);
            requestContext.setProperty("username", claims.get("preferred_username"));
            
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            abortWithUnauthorized(requestContext);
        }
    }

    private Claims validateToken(String token) throws Exception {
        if (publicKey == null) {
            publicKey = fetchPublicKey();
        }
        
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PublicKey fetchPublicKey() throws Exception {
        String certsUrl = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/certs";
        URL url = new URL(certsUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> jwks = objectMapper.readValue(response.toString(), Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
        
        String publicKeyPem = (String) keys.get(0).get("x5c");
        publicKeyPem = ((List<String>) keys.get(0).get("x5c")).get(0);
        
        byte[] decoded = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Unauthorized\"}")
                .build()
        );
    }
}
```

---

### `/api/src/main/java/nl/wateralmanak/model/Voorziening.java`

```java
package nl.wateralmanak.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.OffsetDateTime;
import java.util.UUID;

public class Voorziening {
    
    private UUID id;
    private String naam;
    private String beschrijving;
    private Double longitude;
    private Double latitude;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime updatedAt;

    public Voorziening() {
    }

    public Voorziening(UUID id, String naam, String beschrijving, 
                      Double longitude, Double latitude,
                      OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.naam = naam;
        this.beschrijving = beschrijving;
        this.longitude = longitude;
        this.latitude = latitude;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getBeschrijving() {
        return beschrijving;
    }

    public void setBeschrijving(String beschrijving) {
        this.beschrijving = beschrijving;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

---

### `/api/src/main/java/nl/wateralmanak/repository/VoorzieningRepository.java`

```java
package nl.wateralmanak.repository;

import nl.wateralmanak.config.DatabaseConfig;
import nl.wateralmanak.model.Voorziening;
import org.postgis.PGgeometry;
import org.postgis.Point;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VoorzieningRepository {

    public List<Voorziening> findAll() throws SQLException {
        List<Voorziening> voorzieningen = new ArrayList<>();
        String sql = "SELECT id, naam, beschrijving, ST_X(locatie) as longitude, " +
                    "ST_Y(locatie) as latitude, created_at, updated_at " +
                    "FROM wateralmanak.voorzieningen ORDER BY naam";
        
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                voorzieningen.add(mapResultSetToVoorziening(rs));
            }
        }
        
        return voorzieningen;
    }

    public Optional<Voorziening> findById(UUID id) throws SQLException {
        String sql = "SELECT id, naam, beschrijving, ST_X(locatie) as longitude, " +
                    "ST_Y(locatie) as latitude, created_at, updated_at " +
                    "FROM wateralmanak.voorzieningen WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVoorziening(rs));
                }
            }
        }
        
        return Optional.empty();
    }

    public Voorziening create(Voorziening voorziening) throws SQLException {
        String sql = "INSERT INTO wateralmanak.voorzieningen (naam, beschrijving, locatie) " +
                    "VALUES (?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)) RETURNING id, created_at, updated_at";
        
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, voorziening.getNaam());
            stmt.setString(2, voorziening.getBeschrijving());
            stmt.setDouble(3, voorziening.getLongitude());
            stmt.setDouble(4, voorziening.getLatitude());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    voorziening.setId((UUID) rs.getObject("id"));
                    voorziening.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    voorziening.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                }
            }
        }
        
        return voorziening;
    }

    public Voorziening update(UUID id, Voorziening voorziening) throws SQLException {
        String sql = "UPDATE wateralmanak.voorzieningen " +
                    "SET naam = ?, beschrijving = ?, locatie = ST_SetSRID(ST_MakePoint(?, ?), 4326), " +
                    "updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ? RETURNING updated_at";
        
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, voorziening.getNaam());
            stmt.setString(2, voorziening.getBeschrijving());
            stmt.setDouble(3, voorziening.getLongitude());
            stmt.setDouble(4, voorziening.getLatitude());
            stmt.setObject(5, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    voorziening.setId(id);
                    voorziening.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
                }
            }
        }
        
        return voorziening;
    }

    public boolean delete(UUID id) throws SQLException {
        String sql = "DELETE FROM wateralmanak.voorzieningen WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Voorziening mapResultSetToVoorziening(ResultSet rs) throws SQLException {
        return new Voorziening(
            (UUID) rs.getObject("id"),
            rs.getString("naam"),
            rs.getString("beschrijving"),
            rs.getDouble("longitude"),
            rs.getDouble("latitude"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getObject("updated_at", OffsetDateTime.class)
        );
    }
}
```

---

### `/api/src/main/java/nl/wateralmanak/service/VoorzieningService.java`

```java
package nl.wateralmanak.service;

import nl.wateralmanak.model.Voorziening;
import nl.wateralmanak.repository.VoorzieningRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VoorzieningService {
    
    private final VoorzieningRepository repository = new VoorzieningRepository();

    public List<Voorziening> getAllVoorzieningen() throws SQLException {
        return repository.findAll();
    }

    public Optional<Voorziening> getVoorzieningById(UUID id) throws SQLException {
        return repository.findById(id);
    }

    public Voorziening createVoorziening(Voorziening voorziening) throws SQLException {
        validateVoorziening(voorziening);
        return repository.create(voorziening);
    }

    public Voorziening updateVoorziening(UUID id, Voorziening voorziening) throws SQLException {
        validateVoorziening(voorziening);
        
        if (!repository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Voorziening not found with id: " + id);
        }
        
        return repository.update(id, voorziening);
    }

    public boolean deleteVoorziening(UUID id) throws SQLException {
        return repository.delete(id);
    }

    private void validateVoorziening(Voorziening voorziening) {
        if (voorziening.getNaam() == null || voorziening.getNaam().trim().isEmpty()) {
            throw new IllegalArgumentException("Naam is required");
        }
        
        if (voorziening.getLongitude() == null || voorziening.getLatitude() == null) {
            throw new IllegalArgumentException("Location coordinates are required");
        }
        
        if (voorziening.getLongitude() < -180 || voorziening.getLongitude() > 180) {
            throw new IllegalArgumentException("Invalid longitude");
        }
        
        if (voorziening.getLatitude() < -90 || voorziening.getLatitude() > 90) {
            throw new IllegalArgumentException("Invalid latitude");
        }
    }
}
```

---

### `/api/src/main/java/nl/wateralmanak/resource/VoorzieningResource.java`

```java
package nl.wateralmanak.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.wateralmanak.model.Voorziening;
import nl.wateralmanak.service.VoorzieningService;

import java.util.List;
import java.util.UUID;

@Path("/voorzieningen")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VoorzieningResource {

    private final VoorzieningService service = new VoorzieningService();

    @GET
    public Response getAllVoorzieningen(@Context ContainerRequestContext requestContext) {
        try {
            // Requires 'user' or 'admin' role
            if (!hasRole(requestContext, "user") && !hasRole(requestContext, "admin")) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Insufficient permissions\"}")
                    .build();
            }
            
            List<Voorziening> voorzieningen = service.getAllVoorzieningen();
            return Response.ok(voorzieningen).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getVoorzieningById(@PathParam("id") String id,
                                      @Context ContainerRequestContext requestContext) {
        try {
            // Requires 'user' or 'admin' role
            if (!hasRole(requestContext, "user") && !hasRole(requestContext, "admin")) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Insufficient permissions\"}")
                    .build();
            }
            
            UUID uuid = UUID.fromString(id);
            return service.getVoorzieningById(uuid)
                .map(v -> Response.ok(v).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Voorziening not found\"}")
                    .build());
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Invalid UUID format\"}")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @POST
    public Response createVoorziening(Voorziening voorziening,
                                     @Context ContainerRequestContext requestContext) {
        try {
            // Requires 'admin' role
            if (!hasRole(requestContext, "admin")) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin role required\"}")
                    .build();
            }
            
            Voorziening created = service.createVoorziening(voorziening);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateVoorziening(@PathParam("id") String id,
                                     Voorziening voorziening,
                                     @Context ContainerRequestContext requestContext) {
        try {
            // Requires 'admin' role
            if (!hasRole(requestContext, "admin")) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin role required\"}")
                    .build();
            }
            
            UUID uuid = UUID.fromString(id);
            Voorziening updated = service.updateVoorziening(uuid, voorziening);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteVoorziening(@PathParam("id") String id,
                                     @Context ContainerRequestContext requestContext) {
        try {
            // Requires 'admin' role
            if (!hasRole(requestContext, "admin")) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\": \"Admin role required\"}")
                    .build();
            }
            
            UUID uuid = UUID.fromString(id);
            boolean deleted = service.deleteVoorziening(uuid);
            
            if (deleted) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Voorziening not found\"}")
                    .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Invalid UUID format\"}")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean hasRole(ContainerRequestContext requestContext, String role) {
        Object rolesObj = requestContext.getProperty("roles");
        if (rolesObj instanceof List) {
            return ((List<String>) rolesObj).contains(role);
        }
        return false;
    }
}
```

---

### `/api/src/main/java/nl/wateralmanak/resource/HealthResource.java`

```java
package nl.wateralmanak.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.wateralmanak.config.DatabaseConfig;

import java.sql.Connection;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    public Response health() {
        try {
            // Check database connection
            try (Connection conn = DatabaseConfig.getDataSource().getConnection()) {
                if (conn.isValid(5)) {
                    return Response.ok()
                        .entity("{\"status\": \"UP\", \"database\": \"connected\"}")
                        .build();
                }
            }
            
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("{\"status\": \"DOWN\", \"database\": \"disconnected\"}")
                .build();
                
        } catch (Exception e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("{\"status\": \"DOWN\", \"error\": \"" + e.getMessage() + "\"}")
                .build();
        }
    }
}
```

---

### `/api/src/main/resources/application.properties`

```properties
# Database Configuration
db.url=jdbc:postgresql://postgres:5432/wateralmanak
db.username=wateralmanak_user
db.password=wateralmanak_pass123

# Keycloak Configuration
keycloak.url=http://keycloak:8080
keycloak.realm=wateralmanak
```

---

### `/api/src/main/resources/META-INF/beans.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee 
                           https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd"
       version="3.0"
       bean-discovery-mode="all">
</beans>
```

---

### `/api/src/main/webapp/WEB-INF/web.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                             https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd"
         version="5.0">

    <display-name>Wateralmanak API</display-name>

    <servlet>
        <servlet-name>Jersey Web Application</servlet-name>
        <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
        <init-param>
            <param-name>jakarta.ws.rs.Application</param-name>
            <param-value>nl.wateralmanak.config.ApplicationConfig</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>Jersey Web Application</servlet-name>
        <url-pattern>/api/*</url-pattern>
    </servlet-mapping>

</web-app>
```

---

### `/keycloak/realm-config/wateralmanak-realm.json`

```json
{
  "realm": "wateralmanak",
  "enabled": true,
  "sslRequired": "none",
  "registrationAllowed": false,
  "loginWithEmailAllowed": true,
  "duplicateEmailsAllowed": false,
  "resetPasswordAllowed": true,
  "editUsernameAllowed": false,
  "bruteForceProtected": true,
  "accessTokenLifespan": 3600,
  "roles": {
    "realm": [
      {
        "name": "admin",
        "description": "Administrator role"
      },
      {
        "name": "user",
        "description": "User role"
      }
    ]
  },
  "groups": [
    {
      "name": "admins",
      "path": "/admins",
      "realmRoles": ["admin", "user"]
    },
    {
      "name": "users",
      "path": "/users",
      "realmRoles": ["user"]
    }
  ],
  "users": [
    {
      "username": "admin@wateralmanak.nl",
      "enabled": true,
      "email": "admin@wateralmanak.nl",
      "emailVerified": true,
      "firstName": "Admin",
      "lastName": "User",
      "credentials": [
        {
          "type": "password",
          "value": "admin123",
          "temporary": false
        }
      ],
      "realmRoles": ["admin", "user"],
      "groups": ["/admins"]
    },
    {
      "username": "user@wateralmanak.nl",
      "enabled": true,
      "email": "user@wateralmanak.nl",
      "emailVerified": true,
      "firstName": "Regular",
      "lastName": "User",
      "credentials": [
        {
          "type": "password",
          "value": "user123",
          "temporary": false
        }
      ],
      "realmRoles": ["user"],
      "groups": ["/users"]
    }
  ],
  "clients": [
    {
      "clientId": "wateralmanak-frontend",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": true,
      "standardFlowEnabled": true,
      "implicitFlowEnabled": false,
      "directAccessGrantsEnabled": true,
      "serviceAccountsEnabled": false,
      "redirectUris": [
        "http://localhost/*",
        "http://localhost:4200/*"
      ],
      "webOrigins": [
        "http://localhost",
        "http://localhost:4200"
      ],
      "attributes": {
        "access.token.lifespan": "3600"
      }
    },
    {
      "clientId": "wateralmanak-api",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": false,
      "bearerOnly": true,
      "standardFlowEnabled": false,
      "directAccessGrantsEnabled": false,
      "serviceAccountsEnabled": false
    }
  ]
}
```

---

