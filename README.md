# Court Booking Microservices Application

## Project Overview
A Spring Boot Microservices architecture for a Court Booking Application with:
- Eureka Server (Service Registry)
- API Gateway (Entry point with JWT authentication)
- User Service (User management)
- Booking Service (Court and Booking management)

## Folder Structure

```
CourtBook/
├── pom.xml                          (Parent POM)
├── eureka-server/                   (Service Registry)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/courtbooking/eurekaserver/
│       └── resources/application.yml
├── api-gateway/                     (API Gateway)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/courtbooking/apigateway/
│       │   ├── ApiGatewayApplication.java
│       │   └── config/JwtAuthenticationFilter.java
│       └── resources/application.yml
├── user-service/                    (User Management)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/courtbooking/userservice/
│       │   ├── UserServiceApplication.java
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── entity/
│       │   ├── dto/
│       │   ├── config/
│       │   ├── security/
│       │   └── exception/
│       └── resources/application.yml
├── booking-service/                  (Booking Management)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/courtbooking/bookingservice/
│       │   ├── BookingServiceApplication.java
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── entity/
│       │   ├── dto/
│       │   ├── config/
│       │   └── exception/
│       └── resources/application.yml
└── sql/
    ├── user-service-schema.sql
    └── booking-service-schema.sql
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

## Database Setup

Create databases in MySQL:

```sql
CREATE DATABASE court_booking_users;
CREATE DATABASE court_booking_db;
```

Or use the provided SQL scripts:
- Run `sql/user-service-schema.sql` for User Service database
- Run `sql/booking-service-schema.sql` for Booking Service database

**Note:** Update database credentials in `application.yml` files if needed.

## Build Commands

```bash
# Build all services
mvn clean install

# Build individual service
cd eureka-server && mvn clean install
cd api-gateway && mvn clean install
cd user-service && mvn clean install
cd booking-service && mvn clean install
```

## Run Commands

```bash
# Run Eureka Server
cd eureka-server && mvn spring-boot:run

# Run API Gateway
cd api-gateway && mvn spring-boot:run

# Run User Service
cd user-service && mvn spring-boot:run

# Run Booking Service
cd booking-service && mvn spring-boot:run
```

## API Endpoints

### User Service (Port: 8081)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /auth/register | Register new user | No |
| POST | /auth/login | User login | No |
| GET | /users/{id} | Get user by ID | JWT (ADMIN) |
| GET | /users | Get all users | JWT (ADMIN) |

### Booking Service (Port: 8082)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /courts | Add new court | JWT (ADMIN) |
| PUT | /courts/{id} | Update court | JWT (ADMIN) |
| DELETE | /courts/{id} | Delete court | JWT (ADMIN) |
| GET | /courts | Get all courts | JWT |
| GET | /courts/{id} | Get court by ID | JWT |
| GET | /courts/available | Get available courts | JWT |
| POST | /bookings | Create booking | JWT |
| DELETE | /bookings/{id} | Cancel booking | JWT |
| GET | /bookings/user/{userId} | Get user bookings | JWT |
| GET | /bookings/available?date= | Get available slots | JWT |

## Client Request Flow

All requests must go through API Gateway (Port: 8080):

```
Client -> API Gateway (8080) -> Eureka -> Service
```

### Example: Register User
```
POST http://localhost:8080/auth/register
Content-Type: application/json

{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123"
}
```

### Example: Login
```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "newuser",
    "password": "password123"
}
```

Response:
```json
{
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userId": 1,
    "username": "newuser",
    "email": "newuser@example.com",
    "role": "USER"
}
```

### Example: Create Booking (with JWT)
```
POST http://localhost:8080/bookings
Content-Type: application/json
Authorization: Bearer <token>

{
    "userId": 1,
    "courtId": 1,
    "bookingDate": "2026-04-20",
    "startTime": "10:00:00",
    "endTime": "11:00:00",
    "notes": "Morning practice"
}
```

## Swagger Documentation URLs

| Service | Swagger URL | API Docs URL |
|---------|------------|-------------|
| User Service | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs |
| Booking Service | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |

## Security

- JWT Token Authentication
- BCrypt Password Encryption
- Role-based Authorization (ADMIN/USER)
- Stateless Session Management

## Sample Test Data

### Users (in court_booking_users database)

| Username | Email | Password | Role |
|----------|-------|----------|------|
| admin | admin@courtbooking.com | password123 | ADMIN |
| user1 | user1@courtbooking.com | password123 | USER |
| user2 | user2@courtbooking.com | password123 | USER |

### Courts (in court_booking_db database)

| Name | Sport Type | Location |
|------|------------|----------|
| Court Alpha | Tennis | Ground Floor - Court 1 |
| Court Beta | Badminton | Ground Floor - Court 2 |
| Court Gamma | Basketball | First Floor - Court 1 |
| Court Delta | Volleyball | First Floor - Court 2 |
| Court Epsilon | Tennis | Second Floor - Court 3 |

## Configuration

### application.yml for each service:

**Eureka Server** (application.yml):
```yaml
server:
  port: 8761
spring:
  application:
    name: eureka-server
eureka:
  instance:
    hostname: localhost
  client:
    registerWithEureka: false
    fetchRegistry: false
```

**API Gateway** (application.yml):
```yaml
server:
  port: 8080
spring:
  application:
    name: api-gateway
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
```

**User Service** (application.yml):
```yaml
server:
  port: 8081
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:mysql://localhost:3306/court_booking_users
    username: root
    password: root
```

**Booking Service** (application.yml):
```yaml
server:
  port: 8082
spring:
  application:
    name: booking-service
  datasource:
    url: jdbc:mysql://localhost:3306/court_booking_db
    username: root
    password: root
```

## Notes

1. Start Eureka Server first and wait for it to be ready
2. All services register with Eureka automatically
3. API Gateway routes requests based on path patterns
4. JWT filter validates tokens for protected endpoints
5. Booking Service validates user via REST call to User Service
6. Use RestTemplate for service-to-service communication