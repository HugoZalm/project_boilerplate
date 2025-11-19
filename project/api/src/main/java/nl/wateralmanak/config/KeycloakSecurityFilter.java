package nl.wateralmanak.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
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
            System.out.println("public key loaded: " + publicKey);
        } catch (Exception e) {
            System.err.println("Failed to load public key: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

System.out.println("A");
        String token = authHeader.substring(7);
System.out.println("token: " + token);
        
        try {
            Claims claims = validateToken(token);
System.out.println("B");
            
            // Extract roles from token
            @SuppressWarnings("unchecked")
            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
            if (realmAccess != null) {
System.out.println("C");
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
System.out.println("D");
                
                // Store roles in request context for endpoint authorization
                requestContext.setProperty("roles", roles);
            }
System.out.println("E");
            requestContext.setProperty("username", claims.get("preferred_username"));
System.out.println("requestContext: " + requestContext);
            
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            abortWithUnauthorized(requestContext);
        }
    }

    private Claims validateToken(String token) throws Exception {
        if (publicKey == null) {
            throw new Exception("Public key not loaded");
        }

        // JwtParser parser = Jwts.parser()
        //     .verifyWith(publicKey)
        //     .build();
        // System.out.println("DEBUG: Starting JWT parsing...");

        // Claims claims = parser.parseSignedClaims(token).getPayload();
        // System.out.println("DEBUG: JWT parsed OK!");

        // return claims;

        try {
            JwtParser parser = Jwts.parser()
                .verifyWith(publicKey)
                .build();
            System.out.println("DEBUG: Parser built successfully");
            System.out.println("DEBUG key algorithm: " + publicKey.getAlgorithm());

            Jwt<JwsHeader,Claims> jwt = parser.parseSignedClaims(token);
            System.out.println("DEBUG: Signature OK");

            String kid = jwt.getHeader().getKeyId();
            System.out.println("DEBUG kid: " + kid);
            JwsHeader parsedHeader = jwt.getHeader();
            System.out.println("DEBUG alg: " + parsedHeader.getAlgorithm());
            System.out.println("DEBUG kid: " + parsedHeader.getKeyId());
            System.out.println("DEBUG key algorithm: " + publicKey.getAlgorithm());

            Claims claims = jwt.getPayload();
            System.out.println("DEBUG: Claims parsed: " + claims);
            System.out.println("DEBUG iss: " + claims.getIssuer());
            
            return claims;

        } catch (ExpiredJwtException e) {
            System.err.println("DEBUG: Token expired: " + e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            System.err.println("DEBUG: Token malformed: " + e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            System.err.println("DEBUG: Unsupported JWT: " + e.getMessage());
            throw e;
        } catch (SignatureException | SecurityException e) {
            System.err.println("DEBUG: Signature validation failed: " + e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            System.err.println("DEBUG: Illegal argument: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("DEBUG: Other JWT error: " + e.getMessage());
            throw e;
        }

        // return Jwts.parser()
        //         .verifyWith(publicKey)
        //         .build()
        //         .parseSignedClaims(token)
        //         .getPayload();
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
}
