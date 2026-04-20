package com.campus.api.resources;

import com.campus.api.models.Sensor;
import com.campus.api.models.SensorReading;
import com.campus.api.store.DataStore;
import com.campus.api.exceptions.SensorUnavailableException;
import com.campus.api.http.ApiPayloads;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiPayloads.error(404, "sensor_not_found", "Sensor not found."))
                    .build();
        }

        List<SensorReading> readings = sensor.getReadings() == null
                ? new ArrayList<>()
                : new ArrayList<>(sensor.getReadings());

        return Response.ok(readings).build();
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiPayloads.error(404, "sensor_not_found", "Sensor not found."))
                    .build();
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiPayloads.error(400, "reading_payload_missing", "Reading payload is required."))
                    .build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is currently in maintenance.");
        }

        if (Double.isNaN(reading.getValue()) || Double.isInfinite(reading.getValue())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiPayloads.error(400, "reading_value_invalid", "Reading value must be a finite number."))
                    .build();
        }

        SensorReading persistedReading = new SensorReading();
        persistedReading.setId(UUID.randomUUID().toString());
        persistedReading.setTimestamp(reading.getTimestamp() > 0 ? reading.getTimestamp() : System.currentTimeMillis());
        persistedReading.setValue(reading.getValue());

        synchronized (sensor) {
            if (sensor.getReadings() == null) {
                sensor.setReadings(new ArrayList<>());
            }
            sensor.getReadings().add(persistedReading);
            sensor.setCurrentValue(persistedReading.getValue());
        }

        return Response.status(Response.Status.CREATED)
                .entity(ApiPayloads.created(
                        "Sensor reading added successfully.",
                        "/api/v1/sensors/" + sensorId + "/readings/" + persistedReading.getId(),
                        persistedReading
                ))
                .build();
    }
}