# Court Booking System - Conversation Summary
## Date: 2026-05-14

---

## WHAT WAS DISCUSSED

### 1. Backend Status Analysis
- Identified what was completed vs what was missing
- Found gaps in: JaCoCo, SonarQube, FileAppender, WebClient, diagrams
- Created todo list to track fixes

### 2. Requirements vs Implementation
- Analyzed against case study expectations
- Found ~60% completion status
- Listed all missing items

---

## CHANGES MADE

### 1. pom.xml (Parent) - Added JaCoCo + SonarQube
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <!-- 100% line and branch coverage required -->
</plugin>

<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.11.0.3929</version>
</plugin>
```

### 2. application.yml files - Added FileAppender Logging
- user-service/application.yml
- booking-service/application.yml
- payment-service/application.yml
- api-gateway/application.yml

```yaml
logging:
  file:
    name: logs/[service-name].log
    max-size: 10MB
    max-history: 30
```

### 3. booking-service/AppConfig.java - Replaced RestTemplate with WebClient
- Removed hardcoded service URLs
- Added WebClient bean with baseUrl: http://localhost:8080
- Added async methods for non-blocking calls

### 4. booking-service/UserServiceClient.java - Fixed Lombok Issues
- Replaced @Slf4j with manual Logger import
- Removed unused AppConfig dependency

### 5. booking-service/PaymentServiceClient.java - Fixed Type Safety
- Replaced `bodyToMono(Map.class)` with `bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})`
- Replaced @Slf4j with manual Logger import

### 6. booking-service/application.yml - Removed Hardcoded URLs
- Removed `user-service.url` and `payment-service.url`
- Added `app.user-service-path` and `app.payment-service-path`

### 7. Created Diagrams (in Report/Diagrams/)
- Architecture_Diagram.md
- ER_Diagram.md
- UseCase_Diagram.md
- Sequence_Diagrams.md

### 8. Created Documentation (in Report/Documentation/)
- Complete_File_Documentation.md (line-by-line explanation)
- Quick_Start_Guide.md

---

## SECURITY ARCHITECTURE EXPLAINED

### Centralized Security at API Gateway

**KEY POINTS:**

1. **ONE filter at API Gateway** - NOT at every service
2. **All requests must go through Gateway** - No direct access to services
3. **JWT validated ONLY at Gateway** - Services trust X-User-Id and X-User-Role headers

```
CLIENT → API GATEWAY (JWT Filter) → SERVICE (no filter)
         │
         ├── PUBLIC path? → ALLOW without token
         │
         └── PRIVATE path? → VALIDATE JWT
                                │
                                ├── VALID → Add X-User-Id, X-User-Role headers → Route to service
                                └── INVALID → 401 Unauthorized
```

### Why Only One Filter?

Because ALL services MUST go through the API Gateway. It's like:
- One entrance to a building = One security checkpoint
- Not one at every room

### Request Flow

```
Every request → goes through API Gateway → filter checks:

   ├── PUBLIC path? (/auth/login, /auth/register) → No JWT needed
   └── PRIVATE path? (/courts, /bookings, etc.) → JWT required
```

### Public vs Private Paths

```java
private static final List<String> PUBLIC_PATHS = List.of(
    "/auth/register",     // No token needed
    "/auth/login",        // No token needed
    "/users/validate/",   // Internal call - no token needed
    "/eureka/**",         // Dev tools
    "/actuator/**"        // Health checks
);
```

---

## CENTRALIZED SECURITY UNDERSTANDING

### User's Confirmation (CORRECT):
- ✅ ONE filter at API Gateway (not at every service)
- ✅ NOT a separate authentication service
- ✅ Security is CENTRALIZED at API Gateway

### Why Centralized?

| Approach | Pros | Cons |
|----------|------|------|
| Centralized (Our App) | Single point of control, Easy to audit, Consistent | Slight latency |
| Distributed | Faster | Hard to maintain, Inconsistent |

---

## ALL ENDPOINTS

### API Gateway (Port 8080)
```
PUBLIC (No JWT):
├── POST /auth/register → user-service
├── POST /auth/login → user-service
└── GET /users/validate/{id} → user-service

PRIVATE (JWT Required):
├── /users/** → user-service
├── /courts/** → booking-service
├── /bookings/** → booking-service
└── /api/payments/** → payment-service
```

### User Service (Port 8081)
```
├── POST /auth/register (public)
├── POST /auth/login (public)
├── GET /users/validate/{id} (public)
├── GET /users/{id} (private)
└── GET /users (private)
```

### Booking Service (Port 8082)
```
COURTS:
├── POST /courts (private)
├── GET /courts (private)
├── GET /courts/{id} (private)
├── GET /courts/available (private)
├── PUT /courts/{id} (private)
└── DELETE /courts/{id} (private)

BOOKINGS:
├── POST /bookings (private)
├── GET /bookings (private)
├── GET /bookings/user/{userId} (private)
├── GET /bookings/available (private)
└── DELETE /bookings/{id} (private)
```

### Payment Service (Port 8083)
```
├── POST /api/payments (private)
├── GET /api/payments/{id} (private)
├── POST /api/payments/{id}/process (private)
├── POST /api/payments/{id}/refund (private)
├── GET /api/payments/booking/{id} (private)
├── GET /api/payments/user/{id} (private)
└── GET /api/payments/transaction/{id} (private)
```

---

## SERVICE-TO-SERVICE COMMUNICATION

### Current Implementation
- Uses WebClient (async, non-blocking)
- Base URL: http://localhost:8080 (Gateway)
- No hardcoded service URLs

### Current Calls
```
Booking Service → /users/validate/{id} → User Service (public)
Booking Service → /api/payments → Payment Service (public)
```

### Headers in WebClient
```java
@Bean
public WebClient webClient() {
    return WebClient.builder()
        .baseUrl("http://localhost:8080")
        .defaultHeader("Content-Type", "application/json")
        .build();
}
```

### Current Status
- Internal endpoints are PUBLIC (no JWT needed)
- Works without passing Authorization header
- Future enhancement: Could pass headers for full security

---

## PENDING QUESTION (NOT ANSWERED YET)

### "Can a service-to-service call be intercepted?"

**ISSUE:** When booking-service calls user-service or payment-service:
- Current code does NOT pass Authorization header
- Only passes Content-Type header

**OPTIONS DISCUSSED:**
1. Manual header passing from Controller → Service → WebClient
2. Gateway filters to add headers
3. Keep as-is (internal endpoints are public)

**THIS WAS NOT IMPLEMENTED - SAVED FOR LATER**

---

## FILES MODIFIED

| File | Changes |
|------|---------|
| pom.xml (parent) | Added JaCoCo + SonarQube |
| user-service/application.yml | Added FileAppender |
| booking-service/application.yml | Added FileAppender, removed hardcoded URLs |
| payment-service/application.yml | Added FileAppender |
| api-gateway/application.yml | Added FileAppender |
| booking-service/AppConfig.java | Replaced RestTemplate with WebClient |
| booking-service/UserServiceClient.java | Fixed Lombok, manual Logger |
| booking-service/PaymentServiceClient.java | Fixed Lombok, type safety |
| booking-service/AppConfigTest.java | Updated tests for WebClient |
| Report/Diagrams/Architecture_Diagram.md | Created |
| Report/Diagrams/ER_Diagram.md | Created |
| Report/Diagrams/UseCase_Diagram.md | Created |
| Report/Diagrams/Sequence_Diagrams.md | Created |
| Report/Documentation/Complete_File_Documentation.md | Created |
| Report/Documentation/Quick_Start_Guide.md | Created |

---

## COMMANDS FOR TESTING

```bash
# Build
mvn clean install

# Test with coverage
mvn test

# SonarQube analysis
mvn clean verify sonar:sonar

# Start services (in order)
cd eureka-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd booking-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
```

---

## CURRENT STATUS

| Requirement | Status |
|------------|--------|
| JaCoCo 100% Coverage | ✅ Configured |
| SonarQube | ✅ Configured |
| FileAppender Logging | ✅ Added |
| WebClient (Async) | ✅ Implemented |
| Centralized Security | ✅ Working |
| Diagrams | ✅ Created |
| Documentation | ✅ Created |
| Service-to-Service Header Passing | ⏸️ NOT DONE (pending decision) |

---

## SAVED FOR LATER

**PENDING IMPLEMENTATION:**
- Service-to-service header passing (intercepting internal calls)
- Decision needed: whether internal calls should also validate JWT

---

**End of Conversation - 2026-05-14**