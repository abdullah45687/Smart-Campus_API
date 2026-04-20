package com.campus.api.exceptions.mappers;

import com.campus.api.exceptions.RoomNotEmptyException;
import com.campus.api.http.ApiPayloads;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(ApiPayloads.error(409, "room_not_empty", exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}