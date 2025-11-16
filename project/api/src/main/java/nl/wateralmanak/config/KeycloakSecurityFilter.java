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
