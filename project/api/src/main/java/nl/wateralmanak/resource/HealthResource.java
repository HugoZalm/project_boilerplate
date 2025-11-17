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
