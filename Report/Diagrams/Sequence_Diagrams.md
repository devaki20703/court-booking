# Court Booking System - Sequence Diagrams

## Sequence 1: User Registration Flow

```
┌─────────┐     ┌──────────┐     ┌───────────┐     ┌───────────────┐
│  POSTMAN │────▶│API GATEWAY│────▶│USER-SERVICE│────▶│    MYSQL      │
│ (Client) │     │(Port 8080)│     │(Port 8081) │     │(court_booking │
└─────────┘     └──────────┘     └───────────┘     │   _users)      │
                                                  └───────────────┘

Step 1: User sends registration request
────────────────────────────────────────────────────
POST /auth/register
{
  "username": "john",
  "email": "john@example.com",
  "password": "password123"
}

Step 2: Gateway passes request (no JWT needed for register)
────────────────────────────────────────────────────
Request: POST /auth/register

Step 3: AuthController receives request
────────────────────────────────────────────────────
AuthController.register(RegisterRequest)
    │
    ▼
UserService.register(request)
    │
    ├── Validates username not exists
    ├── Validates email not exists
    │
    ▼
Creates User entity with BCrypt(password)
    │
    ▼
Saves to MySQL users table
    │
    ▼
Generates JWT token
    │
    ▼
Returns AuthResponse with token + user info


DETAILED SEQUENCE:
═══════════════════

POSTMAN                  API GATEWAY           USER-SERVICE            MYSQL
    │                        │                    │                     │
    │──POST /auth/register──▶│                    │                     │
    │                        │────POST /auth/register──────────────────▶│
    │                        │                    │                     │
    │                        │                    │◀──Validate Data───│
    │                        │                    │                     │
    │                        │                    │────INSERT user────▶│
    │                        │                    │                     │
    │                        │                    │◀──User Created────│
    │                        │                    │                     │
    │                        │                    │────BCrypt Hash────▶│
    │                        │                    │                     │
    │◀───────────────────────│◀────────────────────│                     │
    │   201 Created          │                    │                     │
    │   {token, userId, ...} │                    │                     │
```

## Sequence 2: User Login Flow

```
┌─────────┐     ┌──────────┐     ┌───────────┐     ┌───────────────┐
│  POSTMAN │────▶│API GATEWAY│────▶│USER-SERVICE│────▶│    MYSQL      │
└─────────┘     └──────────┘     └───────────┘     └───────────────┘

Step 1: User sends login request
────────────────────────────────────────────────────
POST /auth/login
{
  "usernameOrEmail": "john",
  "password": "password123"
}

Step 2: Gateway validates token NOT required (public path)
────────────────────────────────────────────────────
Gateway skips JWT validation for /auth/login

Step 3: Login processing
────────────────────────────────────────────────────
AuthController.login(LoginRequest)
    │
    ▼
UserService.login(request)
    │
    ├── Find user by username OR email
    │
    ├── Verify password with BCrypt
    │
    ├── Check if account enabled
    │
    ├── Generate JWT token
    │
    ▼
Returns AuthResponse with JWT token


DETAILED SEQUENCE:
═══════════════════

POSTMAN                  API GATEWAY           USER-SERVICE            MYSQL
    │                        │                    │                     │
    │──POST /auth/login─────▶│                    │                     │
    │                        │──[No JWT Check]────│                     │
    │                        │                    │                     │
    │                        │────POST /auth/login────────────────────▶│
    │                        │                    │                     │
    │                        │                    │◀──Find User─────────│
    │                        │                    │                     │
    │                        │                    │◀──BCrypt Verify────│
    │                        │                    │                     │
    │                        │                    │────Generate JWT────▶│
    │                        │                    │                     │
    │◀───────────────────────│◀────────────────────│                     │
    │   200 OK               │                    │                     │
    │   {token: "eyJ..."}    │                    │                     │
```

## Sequence 3: Create Booking (with Payment)

```
┌─────────┐  ┌──────────┐  ┌──────────────┐  ┌───────────┐  ┌──────────┐  ┌────────────┐
│ POSTMAN │─▶│API GATEWAY│─▶│BOOKING-SERVICE│─▶│USER-SERVICE│─▶│MYSQL     │  │PAYMENT-SVC │
└─────────┘  └──────────┘  └──────────────┘  └───────────┘  └──────────┘  └────────────┘
                                                                              │
                                                                              ▼
                                                                         ┌──────────┐
                                                                         │MYSQL     │
                                                                         │(payments)│
                                                                         └──────────┘

Step 1: User sends booking request with JWT
────────────────────────────────────────────────────
POST /bookings
Authorization: Bearer eyJhbGci...
{
  "userId": 1,
  "courtId": 1,
  "bookingDate": "2026-05-20",
  "startTime": "10:00:00",
  "endTime": "11:00:00"
}

Step 2: Gateway validates JWT and routes
────────────────────────────────────────────────────
JWT Filter validates token
Extracts X-User-Id: 1, X-User-Role: USER
Routes to booking-service via lb://booking-service


DETAILED SEQUENCE:
═══════════════════

POSTMAN         GATEWAY        BOOKING-SVC     USER-SVC       MYSQL         PAYMENT-SVC
  │               │                │             │              │               │
  │──POST /bookings (JWT)───────▶│              │              │               │
  │               │                │             │              │               │
  │               │──Validate JWT─│              │              │               │
  │               │──Add Headers──│              │              │               │
  │               │                │             │              │               │
  │               │───────────────│────────────▶│              │               │
  │               │                │             │◀──Validate──│              │
  │               │                │             │──userId=1──▶│              │
  │               │                │             │              │               │
  │               │◀──────────────│◀────────────│              │               │
  │               │   Valid=true   │             │              │               │
  │               │                │             │              │               │
  │               │                │──Check Court│              │               │
  │               │                │◀──Court OK──│              │               │
  │               │                │             │              │               │
  │               │                │──Check Conflicts──┐     │               │
  │               │                │◀──No Conflict──────│     │               │
  │               │                │             │              │               │
  │               │                │────INSERT booking────────▶│               │
  │               │                │◀──Booking ID=5─────────│               │
  │               │                │             │              │               │
  │               │                │─────────────────────────│──POST /payment
  │               │                │             │              │               │
  │               │                │             │              │────INSERT──▶│
  │               │                │             │              │               │
  │               │                │◀─────────────────────────────│Payment Created
  │◀───────────────────────────│             │              │               │
  │   201 Created                │             │              │               │
  │   {id:5, status:CONFIRMED}   │             │              │               │
```

## Sequence 4: Payment Processing

```
┌─────────┐  ┌──────────┐  ┌────────────┐  ┌──────────┐
│ POSTMAN │─▶│API GATEWAY│─▶│PAYMENT-SVC │─▶│MYSQL     │
└─────────┘  └──────────┘  └────────────┘  └──────────┘

Step 1: Process payment request
────────────────────────────────────────────────────
POST /api/payments/1/process
Authorization: Bearer eyJ...

Step 2: Payment service processes
────────────────────────────────────────────────────
PaymentController.processPayment(1)
    │
    ▼
PaymentService.processPayment(paymentId)
    │
    ├── Find payment by ID
    ├── Validate status is PENDING
    ├── Generate transaction ID
    ├── Update status to COMPLETED
    ├── Update payment_date to NOW
    │
    ▼
Returns PaymentResponse


DETAILED SEQUENCE:
═══════════════════

POSTMAN         GATEWAY        PAYMENT-SVC       MYSQL
  │               │                │               │
  │──POST /api/payments/1/process─▶│              │
  │               │                │               │
  │               │────JWT Validate│              │
  │               │                │               │
  │               │────────────────│──Find Payment▶│
  │               │                │◀──PENDING─────│
  │               │                │               │
  │               │                │────UPDATE────▶│
  │               │                │◀──COMPLETED───│
  │               │                │               │
  │◀──────────────────────────────│               │
  │   200 OK                     │               │
  │   {status: COMPLETED}        │               │
```

## Sequence 5: Service Discovery (Eureka)

```
┌──────────┐     ┌───────────┐     ┌───────────────┐
│  SERVICE │────▶│  EUREKA   │◀────│   SERVICES    │
│(startup) │     │  SERVER   │     │  (running)    │
└──────────┘     └───────────┘     └───────────────┘

Step 1: Service starts up
────────────────────────────────────────────────────
1. Service loads configuration
2. Service connects to Eureka at localhost:8761
3. Service registers itself with name + URL
4. Eureka adds service to registry

Step 2: Gateway discovers service
────────────────────────────────────────────────────
1. Gateway receives request for /users/**
2. Gateway checks Eureka for "user-service"
3. Eureka returns service URL
4. Gateway routes request using lb://user-service
5. Eureka load balances between instances


DETAILED SEQUENCE:
═══════════════════

SERVICE            EUREKA SERVER         GATEWAY
   │                    │                  │
   │──Register (name,url)─▶               │
   │◀──ACK──────────────│                 │
   │                    │                  │
   │                    │◀──Fetch Registry─│
   │                    │──Service List───▶│
   │                    │                  │
   │                    │                  │
   │◀─────────────────────────────────────│
   │   lb://user-service resolved          │
```

## Sequence 6: JWT Authentication Flow

```
┌─────────┐     ┌──────────┐     ┌───────────┐
│ POSTMAN │────▶│API GATEWAY│────▶│  SERVICE  │
└─────────┘     └──────────┘     └───────────┘

Step 1: User sends request with JWT
────────────────────────────────────────────────────
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...

Step 2: Gateway JWT Filter processes
────────────────────────────────────────────────────
1. Extract Authorization header
2. Extract token after "Bearer "
3. Parse JWT token
4. Validate signature with secret
5. Check expiration
6. Extract claims (userId, role, username)
7. Add headers X-User-Id, X-User-Role
8. Route to service

Step 3: Service receives request with headers
────────────────────────────────────────────────────
1. Service reads X-User-Id header
2. Service reads X-User-Role header
3. Service performs authorization check
4. Service processes business logic


DETAILED SEQUENCE:
═══════════════════

POSTMAN              GATEWAY              SERVICE              DATABASE
  │                     │                    │                    │
  │──GET /users/1──────▶│                    │                    │
  │  (Bearer token)     │                    │                    │
  │                     │                    │                    │
  │                     │──Extract Header────│                    │
  │                     │──Parse JWT────────│                    │
  │                     │──Validate Signature│                    │
  │                     │──Check Expiration──│                    │
  │                     │                    │                    │
  │                     │──Add X-User-Id:1──│                    │
  │                     │──Add X-User-Role:USER─│                  │
  │                     │                    │                    │
  │                     │────────GET /users/1─│──────────────────▶│
  │                     │                    │                    │
  │                     │                    │◀──User Data───────│
  │◀───────────────────│◀────────────────────│                    │
  │   {user data}       │                    │                    │
```