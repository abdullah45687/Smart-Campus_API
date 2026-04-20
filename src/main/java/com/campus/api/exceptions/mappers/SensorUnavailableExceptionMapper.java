package com.campus.api.exceptions.mappers;

import com.campus.api.exceptions.SensorUnavailableException;
import com.campus.api.http.ApiPayloads;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(ApiPayloads.error(403, "sensor_unavailable", exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}