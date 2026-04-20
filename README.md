# SMART CAMPUS REST API - PROJECT REPORT

**Course**: 5COSC022W Client-Server Architectures  
**Institution**: University of Westminster  
**API Base URL**: `http://localhost:8090/smart-campus-api/api/v1`  
**Technology Stack**: JAX-RS, Jersey, Jackson JSON, `java.util.logging`, Maven

---

## 1. INTRODUCTION

This project implements a RESTful web service for a Smart Campus IoT management system using JAX-RS (Jakarta RESTful Web Services). The API manages rooms, sensors, and sensor readings, and demonstrates robust REST design with resource hierarchy, validation, exception mapping, logging filters, and sub-resource routing.

All data is stored in-memory using thread-safe Java data structures (`ConcurrentHashMap`), with synchronized updates for nested mutable collections where required.

**Key Features:**
- Room management with validation and safe deletion rules.
- Sensor management with optional type-based filtering.
- Nested sensor reading history via sub-resource locator pattern.
- Custom exception hierarchy with precise HTTP status mappings.
- Request/response observability via JAX-RS filters.
- HATEOAS-style discovery root endpoint.

---

## 2. TECHNICAL ARCHITECTURE

### 2.1 Technologies Used
- **JAX-RS 3.x** - Java API for RESTful Web Services
- **Jersey** - Reference implementation of JAX-RS
- **Jackson** - JSON processing and data binding
- **java.util.logging** - Request/response logging
- **Maven** - Build automation and dependency management

### 2.2 Project Structure
```text
src/main/java/com/campus/api/
├── models/                    # Entity classes (Room, Sensor, SensorReading)
├── resources/                 # JAX-RS resource classes (REST endpoints)
├── store/                     # In-memory data store (DataStore.java)
├── exceptions/                # Custom exception hierarchy
│   ├── mappers/               # Exception-to-HTTP mapper classes
│   ├── LinkedResourceNotFoundException.java
│   ├── RoomNotEmptyException.java
│   └── SensorUnavailableException.java
├── filters/                   # API logging filters
└── CampusApplication.java     # JAX-RS application configuration
```

---

## 3. API ENDPOINTS DOCUMENTATION

### 3.1 Metadata & Discovery
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1` | GET | HATEOAS discovery endpoint returning API metadata and primary links |

### 3.2 Room Management
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/rooms` | POST | Create a new room |
| `/api/v1/rooms` | GET | Retrieve all rooms |
| `/api/v1/rooms/{roomId}` | GET | Retrieve one room by ID |
| `/api/v1/rooms/{roomId}` | DELETE | Delete room (blocked if sensors still assigned) |

### 3.3 Sensor Management
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/sensors` | POST | Register sensor (validates `roomId`) |
| `/api/v1/sensors` | GET | Retrieve all sensors (supports `?type=` filter) |
| `/api/v1/sensors/{sensorId}` | GET | Retrieve one sensor by ID |

### 3.4 Deep Nesting: Sensor Readings
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/sensors/{sensorId}/readings` | POST | Append new reading for sensor |
| `/api/v1/sensors/{sensorId}/readings` | GET | Retrieve reading history for sensor |

---

## 4. BUILDING AND RUNNING THE APPLICATION

### 4.1 Prerequisites
- **Java 11** or higher
- **Maven 3.6** or higher

### 4.2 Build Commands
```bash
# Clean and compile
mvn clean compile

# Package as WAR
mvn clean package
```

### 4.3 Running the API
- Deploy `target/smart-campus-api.war` (or your generated WAR artifact) to a local servlet container (Tomcat/Jetty).
- Ensure the context path is `/smart-campus-api` and server port is `8090` (or override with Maven property `jetty.port`).

For quick local execution using Maven Jetty plugin:

```bash
mvn jetty:run
```

### 4.4 Sample API Calls (5 Endpoints)

**1) Discovery endpoint**
```bash
curl -X GET http://localhost:8090/smart-campus-api/api/v1/
```

**2) Create a room**
```bash
curl -X POST http://localhost:8090/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "id": "ENG-101",
    "name": "Engineering Wing",
    "capacity": 50
  }'
```

**3) Retrieve all rooms**
```bash
curl -X GET http://localhost:8090/smart-campus-api/api/v1/rooms
```

**4) Register a sensor linked to the room**
```bash
curl -X POST http://localhost:8090/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TEMP-001",
    "name": "Temperature Sensor T1",
    "type": "TEMPERATURE",
    "status": "ACTIVE",
    "roomId": "ENG-101"
  }'
```

**5) Append nested sensor reading**
```bash
curl -X POST http://localhost:8090/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{
    "value": 23.5,
    "timestamp": 1698141600000
  }'
```

(These are aligned with `Smart_Campus_API_Tests.postman_collection.json`.)

---

## 5. CONCEPTUAL REPORT (QUESTIONS & ANSWERS)

### Part 1: Service Architecture & Setup

**Q1: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.**  
**Answer:** By default, JAX-RS uses a **per-request lifecycle**. A new resource instance is created for each incoming request, then discarded. Because resource instances are short-lived, shared mutable state must not be stored in instance fields. For in-memory persistence across requests, shared collections should be static and thread-safe (for example `ConcurrentHashMap`), and mutation logic must be designed to avoid race conditions.

**Q2: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**  
**Answer:** HATEOAS makes responses self-descriptive by exposing navigational links at runtime (for example links to `/rooms` and `/sensors`). Clients can discover valid next actions dynamically instead of hardcoding every route from static docs. This improves evolvability, because the server can reorganize internal routing while preserving discoverability for clients.

### Part 2: Room Management

**Q3: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**  
**Answer:** Returning full room objects reduces client round-trips and simplifies UI logic, but increases payload size and bandwidth usage. Returning only IDs reduces payload size but often causes extra follow-up requests (`N+1` pattern), increasing client complexity and latency. The best choice depends on dataset size and client usage patterns.

**Q4: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**  
**Answer:** Yes, DELETE is idempotent. The first successful request removes the target room. Repeating the same DELETE does not change system state further (the room remains absent), even though the response may change from success (`204`) to not found (`404`). Idempotency is about final server state, not identical status codes.

### Part 3: Sensor Operations & Linking

**Q5: We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as `text/plain` or `application/xml`. How does JAX-RS handle this mismatch?**  
**Answer:** JAX-RS checks the request `Content-Type` before invoking the resource method. If the payload type is not supported by the method’s `@Consumes`, the runtime rejects the request and typically returns **HTTP 415 Unsupported Media Type**.

**Q6: You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?**  
**Answer:** Query parameters are the conventional REST mechanism for optional filtering over a collection (`/sensors?type=CO2`). They compose naturally for multiple criteria (`?type=CO2&status=ACTIVE`) and keep URI paths clean and stable. Path segments are better suited for resource identity/hierarchy than ad hoc search combinations.

### Part 4: Deep Nesting with Sub-Resources

**Q7: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., `sensors/{id}/readings/{rid}`) in one massive controller class?**  
**Answer:** Sub-resource locators improve modularity by delegating nested concerns (readings) to dedicated classes. This keeps parent resources focused, reduces controller bloat, improves testability, and aligns code structure with URI hierarchy. It is easier to maintain and extend than one monolithic class handling all nested routes.

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**Q8: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**  
**Answer:** `404` means the requested endpoint/resource URI itself was not found. In this case, the endpoint exists and JSON structure is valid, but a referenced entity inside the payload (for example `roomId`) is invalid in the current domain state. `422 Unprocessable Entity` better communicates this semantic validation failure.

**Q9: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**  
**Answer:** Stack traces can leak internal class names, package structure, framework/library versions, file paths, and code flow details. Attackers can use this intelligence to identify vulnerable components and craft targeted exploits. Production APIs should return sanitized error payloads while logging full internal details only on the server side.

**Q10: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `logger.info()` statements inside every single resource method?**  
**Answer:** Filters centralize cross-cutting concerns in one place, ensuring consistent coverage for all endpoints with less duplication. This improves maintainability, reduces human error, and keeps resource methods focused on business logic rather than infrastructural concerns.
