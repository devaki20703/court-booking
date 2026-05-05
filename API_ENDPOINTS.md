# Court Booking Application - API Endpoints

## Access via API Gateway: http://localhost:8080

## User Service APIs

### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | /auth/register | Register new user | No |
| POST | /auth/login | Login and get JWT token | No |

### User Management
| Method | Endpoint | Description | Role Required |
|--------|----------|-------------|---------------|
| GET | /users/{id} | Get user by ID | ADMIN |
| GET | /users | Get all users | ADMIN |

## Booking Service APIs

### Court Management
| Method | Endpoint | Description | Role Required |
|--------|----------|-------------|---------------|
| POST | /courts | Create new court | ADMIN |
| PUT | /courts/{id} | Update court | ADMIN |
| DELETE | /courts/{id} | Delete court | ADMIN |
| GET | /courts | Get all courts | No |
| GET | /courts/{id} | Get court by ID | No |
| GET | /courts/available | Get available courts | No |

### Booking Management
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|----------------|
| POST | /bookings | Create new booking | Yes |
| DELETE | /bookings/{id}?userId={id} | Cancel booking | Yes |
| GET | /bookings/user/{userId} | Get bookings by user | Yes |
| GET | /bookings/available?date={date} | Get available slots | Yes |

## Swagger URLs

- User Service: http://localhost:8081/swagger-ui.html
- Booking Service: http://localhost:8082/swagger-ui.html