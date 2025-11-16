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
