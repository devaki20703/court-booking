# Court Booking System - Architecture Diagram

```
                                    ┌─────────────────────────────────────────────────────────────┐
                                    │                      CLIENTS                                │
                                    │         (Postman / Web Browser / Mobile App)               │
                                    └─────────────────────────────────────┬───────────────────────┘
                                                                          │
                                                                          │ HTTP Requests with JWT Token
                                                                          ▼
                                    ┌─────────────────────────────────────────────────────────────┐
                                    │                      API GATEWAY                             │
                                    │                      (Port 8080)                             │
                                    │  ┌───────────────────────────────────────────────────────┐  │
                                    │  │              JWT VALIDATION FILTER                    │  │
                                    │  │   - Validates Token                                    │  │
                                    │  │   - Extracts User ID & Role                            │  │
                                    │  │   - Adds X-User-Id, X-User-Role headers               │  │
                                    │  └───────────────────────────────────────────────────────┘  │
                                    │  ┌───────────────────────────────────────────────────────┐  │
                                    │  │                   ROUTING CONFIG                       │  │
                                    │  │   /auth/**         → user-service (lb://)            │  │
                                    │  │   /users/**        → user-service (lb://)            │  │
                                    │  │   /courts/**       → booking-service (lb://)        │  │
                                    │  │   /bookings/**     → booking-service (lb://)        │  │
                                    │  │   /api/payments/**→ payment-service (lb://)        │  │
                                    │  └───────────────────────────────────────────────────────┘  │
                                    └─────────────────────────────────────┬───────────────────────┘
                                                                          │
                              ┌─────────────────┼─────────────────┬─────────────────┐
                              │                 │                 │                 │
                              │                 │                 │                 │
                              ▼                 ▼                 ▼                 ▼
                    ┌───────────────────┐ ┌───────────────────┐ ┌───────────────────┐
                    │    USER SERVICE   │ │  BOOKING SERVICE  │ │  PAYMENT SERVICE  │
                    │    (Port 8081)    │ │   (Port 8082)     │ │   (Port 8083)     │
                    │                   │ │                   │ │                   │
                    │  ┌─────────────┐  │ │  ┌─────────────┐  │ │  ┌─────────────┐  │
                    │  │AuthController│ │ │  │CourtController│ │ │  │PaymentController│ │
                    │  └─────────────┘  │ │  └─────────────┘  │ │  └─────────────┘  │
                    │  ┌─────────────┐  │ │  ┌─────────────┐  │ │  ┌─────────────┐  │
                    │  │UserController│ │ │  │BookingController│ │ │  │             │  │
                    │  └─────────────┘  │ │  └─────────────┘  │ │  │             │  │
                    │  ┌─────────────┐  │ │  ┌─────────────┐  │ │  └─────────────┘  │
                    │  │  UserService │  │ │  │CourtService │  │ │                   │
                    │  └─────────────┘  │ │  └─────────────┘  │ │                   │
                    │  ┌─────────────┐  │ │  ┌─────────────┐  │ │                   │
                    │  │BookingService│  │ │  │             │  │ │                   │
                    │  └─────────────┘  │ │  └─────────────┘  │ │                   │
                    │  ┌─────────────┐  │ │  ┌─────────────┐  │ │                   │
                    │  │  JwtService │  │ │  │PaymentClient│  │ │                   │
                    │  └─────────────┘  │ │  │ (WebClient) │  │ │                   │
                    │  ┌─────────────┐  │ │  └─────────────┘  │ │                   │
                    │  │  Repository  │  │ │  ┌─────────────┐  │ │                   │
                    │  └─────────────┘  │ │  │UserServiceClient│ │                   │
                    │                    │ │  │ (WebClient)  │  │                   │
                    │                    │ │  └─────────────┘  │                   │
                    │                    │ │  ┌─────────────┐  │ │                   │
                    │                    │ │  │  Repository  │  │                   │
                    │                    │ │  └─────────────┘  │                   │
                    └────────────────────┼────────────────────┘                   │
                                         │                                         │
                                         └─────────────────┬───────────────────────┘
                                                           │
                                                           ▼
                                              ┌───────────────────────┐
                                              │    EUREKA SERVER     │
                                              │     (Port 8761)       │
                                              │                       │
                                              │  Service Registry    │
                                              │  & Discovery         │
                                              │                       │
                                              │  Registers:          │
                                              │  - user-service      │
                                              │  - booking-service   │
                                              │  - payment-service   │
                                              └───────────────────────┘

                                    ┌─────────────────────────────────────────────────────────────┐
                                    │                       DATABASE LAYER                        │
                                    ├─────────────────────┬─────────────────────┬─────────────────┤
                                    │  court_booking_    │  court_booking_     │  court_booking_ │
                                    │  users              │  db                 │  payments       │
                                    │  ┌─────────────┐   │  ┌─────────────┐   │  ┌─────────────┐ │
                                    │  │    users     │   │  │   courts    │   │  │  payments   │ │
                                    │  └─────────────┘   │  └─────────────┘   │  └─────────────┘ │
                                    │                     │  ┌─────────────┐   │                  │
                                    │                     │  │  bookings   │   │                  │
                                    │                     │  └─────────────┘   │                  │
                                    └─────────────────────┴─────────────────────┴─────────────────┘

                                    ┌─────────────────────────────────────────────────────────────┐
                                    │                         LOGGING                              │
                                    │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐       │
                                    │  │user-service │  │booking-service│ │payment-service│     │
                                    │  │   .log      │  │    .log       │  │    .log       │     │
                                    │  └─────────────┘  └─────────────┘  └─────────────┘       │
                                    └─────────────────────────────────────────────────────────────┘
```

## Service Communication Flow

```
Client Request
     │
     ▼
┌─────────────┐
│ API Gateway │
│  (JWT Auth) │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐     ┌─────────────────┐
│  Service via LB:// │────▶│   Eureka        │
│  (Load Balancer)   │     │   Registry      │
└─────────────────────┘     └────────┬────────┘
                                    │
               ┌────────────────────┼────────────────────┐
               │                    │                    │
               ▼                    ▼                    ▼
       ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
       │  user-service  │   │booking-service │   │payment-service │
       │                │   │                │   │                │
       │ - Register     │   │ - Courts CRUD │   │ - Create Pay  │
       │ - Login/JWT    │   │ - Bookings     │   │ - Process Pay │
       │ - User Mgmt    │   │                │   │ - Refund      │
       └───────────────┘   │  WebClient ────┼──▶│                │
                           │  calls user-svc │   │                │
                           │  calls payment  │   │                │
                           └─────────────────┘   └───────────────┘
```

## Key Features Implemented

| Feature | Implementation |
|---------|---------------|
| JWT Authentication | ApiGateway JwtAuthenticationFilter validates tokens |
| Service Discovery | Eureka client in each service |
| Load Balancing | `lb://service-name` in gateway routes |
| Async Processing | WebClient (non-blocking) for inter-service calls |
| File Logging | logback with file appender in each service |
| Code Coverage | JaCoCo plugin with 100% requirement |
| API Docs | Swagger/OpenAPI via springdoc |
| Security | BCrypt password encryption |