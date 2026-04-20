# Complete Beginner Setup Guide - Smart Campus API (JAX-RS)

## Table of Contents
1. [System Requirements](#system-requirements)
2. [Step 1: Install Java (JDK 11+)](#step-1-install-java-jdk-11)
3. [Step 2: Install Apache Maven](#step-2-install-apache-maven)
4. [Step 3: Verify Installations](#step-3-verify-installations)
5. [Step 4: Get the Project](#step-4-get-the-project)
6. [Step 5: Build and Run the API](#step-5-build-and-run-the-api)
7. [Step 6: Import Postman Collection](#step-6-import-postman-collection)
8. [Step 7: Test the Smart Campus API](#step-7-test-the-smart-campus-api)
9. [Troubleshooting](#troubleshooting)
10. [Next Steps](#next-steps)

---

## System Requirements

This guide works on:
- **Windows 10/11**
- **macOS 10.15+**
- **Linux (Ubuntu/Debian/CentOS)**

Minimum:
- 4 GB RAM (8 GB recommended)
- 2 GB free storage
- Internet access for dependency downloads

---

## Step 1: Install Java (JDK 11+)

This API is built in Java and requires **JDK 11 or newer**.

### Windows
1. Download Temurin JDK 11: https://adoptium.net/temurin/releases/
2. Install and enable `JAVA_HOME` during setup (if prompted).
3. Ensure `%JAVA_HOME%\bin` is in `Path`.

### macOS
Using Homebrew:

```bash
brew install openjdk@11
```

Optional shell setup (`~/.zshrc`):

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@11
export PATH=$JAVA_HOME/bin:$PATH
```

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install openjdk-11-jdk
```

---

## Step 2: Install Apache Maven

Maven builds and runs this project.

### Windows
1. Download Maven binary zip: https://maven.apache.org/download.cgi
2. Extract and set `MAVEN_HOME`.
3. Add `%MAVEN_HOME%\bin` to `Path`.

### macOS

```bash
brew install maven
```

### Linux

```bash
sudo apt install maven
```

---

## Step 3: Verify Installations

Open a new terminal and run:

```bash
java -version
javac -version
mvn -version
```

You should see Java 11+ and Maven installed correctly.

---

## Step 4: Get the Project

### Option A: Git clone

```bash
git clone <your-repository-url>
cd Smart-Campus-Api-Jax-RS
```

### Option B: ZIP download
- Download ZIP from GitHub
- Extract folder
- Open terminal in extracted directory

Project root should contain:
- `pom.xml`
- `src/`
- `README.md`
- `Smart_Campus_API_Tests.postman_collection.json`

---

## Step 5: Build and Run the API

### 1) Build

```bash
mvn clean compile
mvn clean package
```

### 2) Run locally with Jetty plugin

```bash
mvn jetty:run
```

If successful, API is available at:

- **Base URL:** `http://localhost:8080/smart-campus-api/api/v1`

### 3) Stop server
Press `Ctrl + C` in the terminal running Jetty.

---

## Step 6: Import Postman Collection

1. Open Postman
2. Click **Import**
3. Select file:
   - `Smart_Campus_API_Tests.postman_collection.json`
4. Import

You should see the collection with folders:
- Discovery
- Rooms
- Sensors
- Sensor Readings

---

## Step 7: Test the Smart Campus API

Make sure server is running first (`mvn jetty:run`).

### A) Discovery
- `GET /api/v1/`

Expected: API metadata / links.

### B) Room flow
1. `POST /api/v1/rooms` (create room)
2. `GET /api/v1/rooms` (list rooms)
3. `GET /api/v1/rooms/{roomId}` (single room)

### C) Sensor flow
1. `POST /api/v1/sensors` with existing `roomId`
2. `GET /api/v1/sensors`
3. `GET /api/v1/sensors?type=TEMPERATURE`

### D) Sensor readings flow
1. `POST /api/v1/sensors/{sensorId}/readings`
2. `GET /api/v1/sensors/{sensorId}/readings`

> Note: reading `id` is now **server-generated UUID**. You only need to send `value` and optional `timestamp`.

Sample reading body:

```json
{
  "value": 23.5,
  "timestamp": 1698141600000
}
```

### E) Error case checks (required by coursework)
- `409 Conflict`: delete room with active sensors
- `422 Unprocessable Entity`: create sensor with non-existing room
- `403 Forbidden`: post reading to sensor in `MAINTENANCE`
- `500 Internal Server Error`: handled by global mapper

---

## Troubleshooting

### 1) `java` or `mvn` not found
- Re-check installation steps
- Restart terminal
- Re-check `JAVA_HOME`, `MAVEN_HOME`, and `PATH`

### 2) Port `8080` already in use

Find process using port:

```bash
lsof -i :8080
```

Then either stop that app, or run your server after freeing the port.

### 3) Postman says connection refused
- Confirm `mvn jetty:run` is active
- Confirm URL is exactly:
  - `http://localhost:8080/smart-campus-api/api/v1`

### 4) Build failure in Maven

```bash
mvn clean
mvn -U clean compile
```

---

## Next Steps

### Project structure (high-level)

```text
src/main/java/com/campus/api/
├── CampusApplication.java
├── models/
│   ├── Room.java
│   ├── Sensor.java
│   └── SensorReading.java
├── resources/
│   ├── DiscoveryResource.java
│   ├── SensorRoomResource.java
│   ├── SensorResource.java
│   └── SensorReadingResource.java
├── exceptions/
│   ├── RoomNotEmptyException.java
│   ├── LinkedResourceNotFoundException.java
│   ├── SensorUnavailableException.java
│   └── mappers/
└── filters/
    └── LoggingFilter.java
```

### Recommended testing order
1. Create room
2. Create sensor linked to room
3. Add readings
4. Fetch reading history
5. Validate error scenarios

---

## You’re all set 🎉

You now have a complete setup path for the **Smart Campus JAX-RS API**, with correct URLs, commands, and Postman flow aligned to your coursework project.
