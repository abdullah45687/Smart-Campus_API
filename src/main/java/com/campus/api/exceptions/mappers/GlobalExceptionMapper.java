package com.campus.api.exceptions.mappers;

import com.campus.api.http.ApiPayloads;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.log(Level.SEVERE, "Unhandled exception reached GlobalExceptionMapper", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ApiPayloads.error(500, "internal_server_error", "An unexpected error occurred."))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}