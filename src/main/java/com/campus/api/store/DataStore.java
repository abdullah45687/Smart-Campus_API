package com.campus.api.store;

import com.campus.api.models.Room;
import com.campus.api.models.Sensor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private DataStore() {
        // Utility class.
    }

    public static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
}
