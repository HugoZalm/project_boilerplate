package nl.wateralmanak.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Principal;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class KeycloakSecurityFilter implements ContainerRequestFilter {

    private static final String REALM = System.getenv()
        .getOrDefault("KEYCLOAK_REALM", "wateralmanak");
    private static PublicKey publicKey;

    static {
        try {
            System.out.println("try to load public key");
            publicKey = loadPublicKeyFromFile();
            System.out.println("public key loaded");
        } catch (Exception e) {
            System.err.println("Failed to load public key: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        
        boolean isOptionRequest = checkOptionRequest(requestContext);
        boolean isPublicEndpoint = checkPublicEndpoint();
        String authHeader = requestContext.getHeaderString("Authorization");
        boolean hasBearerToken = checkBearerToken(authHeader);

        if (isOptionRequest || isPublicEndpoint) {
            return;
        } else if (!isPublicEndpoint && !hasBearerToken) {
            abortWithUnauthorized(requestContext);
            return;
        }

        String token = authHeader.substring(7);
        
        try {
            Claims claims = validateToken(token);
            setupSecurityContext(requestContext, claims);
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            abortWithUnauthorized(requestContext);
        }
    }
 
    private boolean checkOptionRequest(ContainerRequestContext requestContext) {
        // String path = requestContext.getUriInfo().getPath();
        // return (path.equals("health") || requestContext.getMethod().equals("OPTIONS"));
        return requestContext.getMethod().equals("OPTIONS");
    }

    private boolean checkPublicEndpoint() {
        return resourceInfo.getResourceMethod().isAnnotationPresent(PublicEndpoint.class)
            || resourceInfo.getResourceClass().isAnnotationPresent(PublicEndpoint.class);
    }

    private boolean checkBearerToken(String authHeader) {
        return (authHeader != null && authHeader.startsWith("Bearer "));
    }

    private Claims validateToken(String token) throws Exception {
        if (publicKey == null) {
            throw new Exception("Public key not loaded");
        }

        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private ContainerRequestContext setupSecurityContext(ContainerRequestContext requestContext, Claims claims) {
        @SuppressWarnings("unchecked")
        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
        
        if (realmAccess != null) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            String userName = claims.getSubject();
            SecurityContext originalContext = requestContext.getSecurityContext();
            SecurityContext securityContext = createSecurityContext(roles, userName, originalContext);
            requestContext.setSecurityContext(securityContext);
        }
        requestContext.setProperty("username", claims.get("preferred_username"));
        return requestContext;
    }


    private static PublicKey loadPublicKeyFromFile() throws Exception {
        // Try to load from file first
        InputStream is = KeycloakSecurityFilter.class
            .getResourceAsStream("/keycloak-public-key.txt");
        
        if (is == null) {
            // Fallback to environment variable
            String publicKeyPem = System.getenv("KEYCLOAK_PUBLIC_KEY");
            if (publicKeyPem == null || publicKeyPem.isEmpty()) {
                throw new Exception("Public key not found in file or environment variable");
            }
            return parsePublicKey(publicKeyPem);
        }
        
        String publicKeyPem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        is.close();
        
        return parsePublicKey(publicKeyPem);
    }

    private static PublicKey parsePublicKey(String publicKeyPem) throws Exception {
        // Remove PEM headers and whitespace
        String publicKeyContent = publicKeyPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
        
        byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
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

    private SecurityContext createSecurityContext(List<String> roles, String userName, SecurityContext originalContext) {
        SecurityContext securityContext = new SecurityContext() {

            @Override
            public Principal getUserPrincipal() {
                return () -> userName;
            }

            @Override
            public boolean isUserInRole(String role) {
                return roles.contains(role);
            }

            @Override
            public boolean isSecure() {
                return originalContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return "Bearer";
            }
        };
        return securityContext;
    }
}
