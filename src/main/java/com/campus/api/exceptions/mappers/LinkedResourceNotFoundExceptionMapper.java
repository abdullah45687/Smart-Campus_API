package com.campus.api.exceptions.mappers;

import com.campus.api.exceptions.LinkedResourceNotFoundException;
import com.campus.api.http.ApiPayloads;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        return Response.status(422) // Unprocessable Entity
                .entity(ApiPayloads.error(422, "linked_resource_not_found", exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}