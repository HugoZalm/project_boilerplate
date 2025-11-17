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
