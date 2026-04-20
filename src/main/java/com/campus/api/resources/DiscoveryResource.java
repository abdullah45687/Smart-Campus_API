package com.campus.api.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscoveryInfo(@Context UriInfo uriInfo) {
    String baseUrl = uriInfo.getBaseUri().toString();

    Map<String, Object> info = new LinkedHashMap<>();
    info.put("service", "Smart Campus Sensor & Room API");
        info.put("version", "v1");

    Map<String, String> contact = new LinkedHashMap<>();
    contact.put("team", "Campus Platform Engineering");
    contact.put("email", "api-admin@westminster-campus.local");
    info.put("contact", contact);

    Map<String, Object> resources = new LinkedHashMap<>();
    resources.put("rooms", Map.of(
        "href", baseUrl + "rooms",
        "methods", List.of("GET", "POST")
    ));
    resources.put("sensors", Map.of(
        "href", baseUrl + "sensors",
        "methods", List.of("GET", "POST")
    ));
    resources.put("sensorReadingsTemplate", Map.of(
        "href", baseUrl + "sensors/{sensorId}/readings",
        "methods", List.of("GET", "POST")
    ));

    info.put("resources", resources);
    info.put("generatedAt", Instant.now().toString());
        return Response.ok(info).build();
    }
}