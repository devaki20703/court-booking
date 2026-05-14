# Court Booking System - Use Case Diagram

## Actors

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           ACTORS                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────┐      ┌─────────────┐      ┌─────────────┐               │
│   │   ADMIN     │      │    USER     │      │   SYSTEM    │               │
│   │             │      │             │      │  (Backend)  │               │
│   ├─────────────┤      ├─────────────┤      ├─────────────┤               │
│   │ - Manage    │      │ - Register  │      │ - Validate  │               │
│   │   Courts    │      │ - Login     │      │   Users     │               │
│   │ - View All  │      │ - View      │      │ - Process   │               │
│   │   Bookings  │      │   Courts    │      │   Payments  │               │
│   │ - View All  │      │ - Create    │      │ - Auto       │               │
│   │   Users     │      │   Bookings  │      │   Payments  │               │
│   │             │      │ - Cancel    │      │             │               │
│   │             │      │   Bookings  │      │             │               │
│   └─────────────┘      └─────────────┘      └─────────────┘               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Use Cases

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           USE CASE DIAGRAM                                   │
└─────────────────────────────────────────────────────────────────────────────┘

                                    ┌─────────────────────────────┐
                                    │     COURT BOOKING SYSTEM     │
                                    │                              │
                                    │  ┌─────────────────────────┐ │
                                    │  │ UC1: User Registration   │ │
                                    │  └────────────┬────────────┘ │
                                    │               │              │
                                    │  ┌────────────▼────────────┐ │
                                    │  │ UC2: User Login          │ │
                                    │  └────────────┬────────────┘ │
                                    │               │              │
                                    │  ┌────────────▼────────────┐ │
                                    │  │ UC3: View Available     │ │
                                    │  │      Courts             │ │
                                    │  └────────────┬────────────┘ │
                                    │               │              │
                                    │  ┌────────────▼────────────┐ │
                                    │  │ UC4: Create Booking      │ │
                                    │  └────────────┬────────────┘ │
                                    │               │              │
                                    │  ┌────────────▼────────────┐ │
                                    │  │ UC5: Cancel Booking      │ │
                                    │  └────────────┬────────────┘ │
                                    │               │              │
                                    │  ┌────────────▼────────────┐ │
                                    │  │ UC6: Process Payment     │ │
                                    │  └────────────┬────────────┘ │
                                    │               │              │
                                    │  ┌────────────▼────────────┐ │
                                    │  │ UC7: View My Bookings   │ │
                                    │  └─────────────────────────┘ │
                                    └─────────────────────────────┘

                              ADMIN ONLY USE CASES
                              (Requires ADMIN role)

                                    ┌─────────────────────────────┐
                                    │  ┌─────────────────────────┐ │
                                    │  │ UC8: Add Court           │ │
                                    │  └────────────┬────────────┘ │
                                    │               │              │
                                    │  ┌────────────▼────────────┐ │
                                    │  │ UC9: Update Court        │ │
                                    │  └────────────┬────────────┘ │
                                    │               │              │
                                    │  ┌────────────▼────────────┐ │
                                    │  │ UC10: Delete Court       │ │
                                    │  └────────────┬────────────┘ │
                                    │               │              │
                                    │  ┌────────────▼────────────┐ │
                                    │  │ UC11: View All Users     │ │
                                    │  └────────────┬────────────┘ │
                                    │               │              │
                                    │  ┌────────────▼────────────┐ │
                                    │  │ UC12: View All Bookings  │ │
                                    │  └─────────────────────────┘ │
                                    └─────────────────────────────┘
```

## Use Case Descriptions

### UC1: User Registration
| Field | Description |
|-------|-------------|
| **Actor** | Guest (unauthenticated user) |
| **Trigger** | User submits registration form |
| **Pre-condition** | User does not have an account |
| **Post-condition** | New user account created, JWT token returned |
| **Flow** | 1. User provides username, email, password |
| | 2. System validates data |
| | 3. System encrypts password with BCrypt |
| | 4. System saves user to database |
| | 5. System generates JWT token |
| | 6. System returns token + user info |

### UC2: User Login
| Field | Description |
|-------|-------------|
| **Actor** | Registered user |
| **Trigger** | User submits login form |
| **Pre-condition** | User has valid account |
| **Post-condition** | JWT token returned if credentials valid |
| **Flow** | 1. User provides username/email and password |
| | 2. System verifies credentials |
| | 3. System checks if account is enabled |
| | 4. System generates JWT token |
| | 5. System returns token + user info |

### UC3: View Available Courts
| Field | Description |
|-------|-------------|
| **Actor** | Authenticated user |
| **Trigger** | User requests court list |
| **Pre-condition** | User has valid JWT token |
| **Post-condition** | List of available courts returned |
| **Flow** | 1. User sends GET /courts with JWT |
| | 2. Gateway validates JWT |
| | 3. Gateway routes to booking-service |
| | 4. Booking service returns courts |

### UC4: Create Booking
| Field | Description |
|-------|-------------|
| **Actor** | Authenticated user |
| **Trigger** | User submits booking request |
| **Pre-condition** | User has valid JWT, court is available |
| **Post-condition** | Booking created, payment initiated |
| **Flow** | 1. User sends POST /bookings with JWT |
| | 2. Gateway validates JWT, extracts user ID |
| | 3. Booking service validates user (via WebClient) |
| | 4. Booking service checks court availability |
| | 5. Booking service checks time slot conflicts |
| | 6. Booking service creates booking |
| | 7. Booking service calls payment service (via WebClient) |
| | 8. System returns booking + payment info |

### UC5: Cancel Booking
| Field | Description |
|-------|-------------|
| **Actor** | Booking owner (or ADMIN) |
| **Trigger** | User sends DELETE /bookings/{id} |
| **Pre-condition** | Booking exists and belongs to user |
| **Post-condition** | Booking status changed to CANCELLED |
| **Flow** | 1. User sends DELETE with JWT |
| | 2. Gateway validates JWT |
| | 3. Booking service verifies ownership |
| | 4. Booking status updated to CANCELLED |
| | 5. Confirmation returned |

### UC6: Process Payment
| Field | Description |
|-------|-------------|
| **Actor** | System (automatic) or User |
| **Trigger** | Payment created or user initiates |
| **Pre-condition** | Payment exists with PENDING status |
| **Post-condition** | Payment status changed to COMPLETED/REFUNDED |
| **Flow** | 1. Payment service receives request |
| | 2. Payment service validates payment |
| | 3. Payment service generates transaction ID |
| | 4. Payment status updated to COMPLETED |
| | 5. Confirmation returned |

### UC7: View My Bookings
| Field | Description |
|-------|-------------|
| **Actor** | Authenticated user |
| **Trigger** | User requests their bookings |
| **Pre-condition** | User has valid JWT |
| **Post-condition** | List of user's bookings returned |
| **Flow** | 1. User sends GET /bookings/user/{userId} |
| | 2. Gateway validates JWT |
| | 3. Booking service queries bookings for user |
| | 4. List of bookings returned |

### UC8-12: Admin Use Cases
| Use Case | Description |
|----------|-------------|
| UC8: Add Court | Admin creates new court |
| UC9: Update Court | Admin modifies court details |
| UC10: Delete Court | Admin removes court |
| UC11: View All Users | Admin lists all users |
| UC12: View All Bookings | Admin lists all bookings |

## Actor-Use Case Relationships

```
         ┌─────────────┐
         │    ADMIN     │
         └──────┬──────┘
                │
    ┌────────────┼────────────┐
    │            │            │
    ▼            ▼            ▼
  UC1          UC4          UC8
  UC2          UC5          UC9
  UC3          UC6          UC10
  UC7          UC11         UC12

         ┌─────────────┐
         │    USER     │
         └──────┬──────┘
                │
    ┌───────────┼───────────┐
    │           │           │
    ▼           ▼           ▼
  UC1          UC3         UC4
  UC2          UC7         UC5
                           UC6
```