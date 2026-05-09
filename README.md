# Court Booking Microservices Application

## Project Overview
A Spring Boot Microservices architecture for a Court Booking Application with:
- **Eureka Server** (Service Registry) - Port 8761
- **API Gateway** (Entry point with JWT authentication) - Port 8080
- **User Service** (User management) - Port 8081
- **Booking Service** (Court and Booking management) - Port 8082
- **Payment Service** (Payment processing) - Port 8083
- **Common** (Shared AOP aspects and configuration)

## Architecture

```
                    ┌─────────────────┐
                    │   Client App    │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │  API Gateway    │ ← JWT Validation
                    │   (Port 8080)   │   Security Config
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
         ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│  User Service   │ │ Booking Service │ │ Payment Service │
│   (Port 8081)   │ │   (Port 8082)   │ │   (Port 8083)   │
└────────┬────────┘ └────────┬────────┘ └────────┬────────┘
         │                   │                   │
         └───────────────────┴───────────────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ Eureka Server   │
                    │  (Port 8761)    │
                    └─────────────────┘
```

### Key Changes (v2)
- **Security**: JWT authentication moved to API Gateway (centralized)
- **Payment Integration**: Auto-creates payment when booking is made
- **Common Module**: Shared AOP for logging, performance, exceptions

## Folder Structure

```
CourtBook/
├── pom.xml                          (Parent POM)
├── common/                          (Shared AOP configuration)
│   ├── pom.xml
│   └── src/main/java/com/courtbooking/common/
│       ├── config/AopConfig.java
│       └── aspect/
│           ├── LoggingAspect.java
│           ├── PerformanceAspect.java
│           ├── ExceptionAspect.java
│           └── TraceAspect.java
├── eureka-server/                   (Service Registry)
│   └── src/main/resources/application.yml
├── api-gateway/                     (API Gateway - Security)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/courtbooking/apigateway/
│       │   ├── ApiGatewayApplication.java
│       │   └── config/
│       │       ├── JwtAuthenticationFilter.java  (JWT validation)
│       │       ├── GatewayAuthorizationFilter.java (Role-based access)
│       │       ├── JwtUtil.java
│       │       └── SecurityConfig.java (WebFlux security)
│       └── resources/application.yml
├── user-service/                    (User Management - Port 8081)
│   └── src/main/java/com/courtbooking/userservice/
│       ├── controller/
│       ├── service/
│       ├── dto/
│       ├── entity/
│       ├── repository/
│       ├── config/ (EncoderConfig)
│       └── exception/
├── booking-service/                 (Booking Management - Port 8082)
│   └── src/main/java/com/courtbooking/bookingservice/
│       ├── controller/
│       ├── service/
│       │   └── PaymentServiceClient.java (Payment integration)
│       ├── dto/
│       ├── entity/
│       ├── repository/
│       └── exception/
└── payment-service/                (Payment Processing - Port 8083)
    └── src/main/java/com/courtbooking/paymentservice/
        ├── controller/
        ├── service/
        ├── dto/
        ├── entity/
        └── repository/
```

## Prerequisites
- Java 17
- Maven 3.8+
- MySQL 8.0+
- IDE (IntelliJ/Eclipse/VS Code)

## Run Order (Important!)

Start services in the following order:

1. **Eureka Server** (Port: 8761)
2. **API Gateway** (Port: 8080)
3. **User Service** (Port: 8081)
4. **Booking Service** (Port: 8082)
5. **Payment Service** (Port: 8083)

## Database Setup

Create databases in MySQL:

```sql
CREATE DATABASE court_booking_users;
CREATE DATABASE court_booking_db;
CREATE DATABASE court_booking_payments;
```

## Build & Run Commands

```bash
# Build all services
mvn clean install

# Run each service
cd eureka-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd booking-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
```

## API Endpoints

### Through API Gateway (Port 8080)

**User Service:**
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /auth/register | Register new user | No |
| POST | /auth/login | User login | No |
| GET | /users/{id} | Get user by ID | JWT |
| GET | /users | Get all users | JWT |

**Booking Service:**
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /courts | Add new court | JWT (ADMIN) |
| PUT | /courts/{id} | Update court | JWT (ADMIN) |
| GET | /courts | Get all courts | JWT |
| GET | /courts/{id} | Get court by ID | JWT |
| GET | /courts/available | Get available courts | JWT |
| POST | /bookings | Create booking | JWT |
| DELETE | /bookings/{id}?userId= | Cancel booking | JWT |
| GET | /bookings/user/{userId} | Get user bookings | JWT |
| GET | /bookings/available?date= | Get available slots | JWT |

**Payment Service:**
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/payments/user/{userId} | Get payments by user | JWT |
| GET | /api/payments/{id} | Get payment by ID | JWT |
| POST | /api/payments/{id}/process | Process payment | JWT |
| POST | /api/payments/{id}/refund | Refund payment | JWT |

## Security Architecture

### Before (Original):
- Each service has its own Spring Security
- Each service has JwtAuthenticationFilter
- Each service validates JWT independently

### After (Current):
- **API Gateway** handles all JWT validation
- Services receive headers: `X-User-Id`, `X-Username`, `X-User-Role`
- Services are stripped of Spring Security (no SecurityConfig)
- Centralized security management

### JWT Flow:
```
1. Client sends request with JWT
2. API Gateway validates JWT via JwtAuthenticationFilter
3. If valid, adds headers: X-User-Id, X-User-Role, X-Username
4. Forwards request to downstream service
5. Service uses headers for authorization (future)
```

## Payment Integration Flow

```
User creates booking
       │
       ▼
BookingService validates user via UserServiceClient
       │
       ▼
Court available? ──No──→ Return error
       │
      Yes
       ▼
Calculate amount (hours × pricePerHour)
       │
       ▼
Amount > 0? ──No──→ Skip payment, booking saved
       │
     Yes
       ▼
PaymentServiceClient.createPayment()
       │
       ▼
Payment saved with PENDING status
       │
       ▼
Payment auto-processed → COMPLETED
```

## Example Requests

### Register:
```bash
POST http://localhost:8080/auth/register
{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123"
}
```

### Login:
```bash
POST http://localhost:8080/auth/login
{
    "usernameOrEmail": "newuser",
    "password": "password123"
}
```

### Create Booking (with JWT):
```bash
POST http://localhost:8080/bookings
Authorization: Bearer <token>
{
    "userId": 1,
    "courtId": 1,
    "bookingDate": "2026-05-20",
    "startTime": "10:00",
    "endTime": "11:00"
}
```

## Swagger Documentation

| Service | URL |
|---------|-----|
| User Service | http://localhost:8081/swagger-ui.html |
| Booking Service | http://localhost:8082/swagger-ui.html |
| Payment Service | http://localhost:8083/swagger-ui.html |

## Configuration

### API Gateway (application.yml):
```yaml
spring:
  main:
    web-application-type: reactive  # Required for WebFlux
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/auth/**,/users/**
        - id: booking-service
          uri: lb://booking-service
          predicates:
            - Path=/courts/**,/bookings/**
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/payments/**
```

### JWT Secret (in application.yml):
```yaml
app:
  jwt:
    secret: courtBookingSecretKey2024VeryLongSecretKeyForJWTTokenGeneration
    expiration: 86400000  # 24 hours
```

## Notes

1. Start Eureka Server first and wait for it to be ready
2. All services register with Eureka automatically
3. API Gateway handles JWT validation centrally
4. Payment is automatically created when a booking has a fee
5. Services communicate via RestTemplate
6. Each service has its own database (users, bookings, payments)