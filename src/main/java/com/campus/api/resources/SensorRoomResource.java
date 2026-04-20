package com.campus.api.resources;

import com.campus.api.exceptions.RoomNotEmptyException;
import com.campus.api.http.ApiPayloads;
import com.campus.api.models.Room;
import com.campus.api.store.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    @GET
    public Response getRooms() {
        ArrayList<Room> rooms = new ArrayList<>(DataStore.rooms.values());
        rooms.sort(Comparator.comparing(Room::getId, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return Response.ok(rooms).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiPayloads.error(400, "room_payload_missing", "Room payload is required."))
                    .build();
        }

        String roomId = normalize(room.getId());
        if (roomId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiPayloads.error(400, "room_id_missing", "Room id must be provided."))
                    .build();
        }

        room.setId(roomId);
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        Room existing = DataStore.rooms.putIfAbsent(roomId, room);
        if (existing != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiPayloads.error(409, "room_already_exists", "A room with this id already exists."))
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(ApiPayloads.created("Room created successfully.", "/api/v1/rooms/" + roomId, room))
                .build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiPayloads.error(404, "room_not_found", "Room not found."))
                    .build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiPayloads.error(404, "room_not_found", "Room not found."))
                    .build();
        }

        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + roomId + " cannot be deleted as it has active sensors.");
        }

        DataStore.rooms.remove(roomId);
        return Response.noContent().build();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}