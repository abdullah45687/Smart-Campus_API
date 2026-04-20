package com.campus.api.resources;

import com.campus.api.exceptions.LinkedResourceNotFoundException;
import com.campus.api.http.ApiPayloads;
import com.campus.api.models.Room;
import com.campus.api.models.Sensor;
import com.campus.api.store.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        String normalizedType = normalize(type);
        List<Sensor> result = new ArrayList<>(DataStore.sensors.values());
        if (normalizedType != null) {
            result = result.stream()
                    .filter(sensor -> sensor.getType() != null && normalizedType.equalsIgnoreCase(sensor.getType()))
                    .collect(Collectors.toList());
        }

        result.sort(Comparator.comparing(Sensor::getId, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return Response.ok(result).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiPayloads.error(400, "sensor_payload_missing", "Sensor payload is required."))
                    .build();
        }

        String sensorId = normalize(sensor.getId());
        if (sensorId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiPayloads.error(400, "sensor_id_missing", "Sensor id must be provided."))
                    .build();
        }

        String roomId = normalize(sensor.getRoomId());
        if (roomId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiPayloads.error(400, "sensor_room_missing", "roomId is required for sensor registration."))
                    .build();
        }

        if (!DataStore.rooms.containsKey(roomId)) {
            throw new LinkedResourceNotFoundException("Room ID " + roomId + " does not exist.");
        }

        sensor.setId(sensorId);
        sensor.setRoomId(roomId);
        if (normalize(sensor.getStatus()) == null) {
            sensor.setStatus("ACTIVE");
        }
        if (sensor.getReadings() == null) {
            sensor.setReadings(new ArrayList<>());
        }

        Sensor existing = DataStore.sensors.putIfAbsent(sensorId, sensor);
        if (existing != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiPayloads.error(409, "sensor_already_exists", "A sensor with this id already exists."))
                    .build();
        }

        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            DataStore.sensors.remove(sensorId);
            throw new LinkedResourceNotFoundException("Room ID " + roomId + " does not exist.");
        }

        synchronized (room) {
            if (room.getSensorIds() == null) {
                room.setSensorIds(new ArrayList<>());
            }
            if (!room.getSensorIds().contains(sensorId)) {
                room.getSensorIds().add(sensorId);
            }
        }

        return Response.status(Response.Status.CREATED)
                .entity(ApiPayloads.created("Sensor registered successfully.", "/api/v1/sensors/" + sensorId, sensor))
                .build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiPayloads.error(404, "sensor_not_found", "Sensor not found."))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}