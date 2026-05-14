# Court Booking System - Quick Start Guide

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- IntelliJ IDEA (recommended) or any IDE

---

## Step 1: Database Setup

Create three separate databases (one per service):

```sql
CREATE DATABASE court_booking_users;
CREATE DATABASE court_booking_db;
CREATE DATABASE court_booking_payments;
```

---

## Step 2: Build the Project

From the `CourtBook` directory:

```bash
# Clean and build everything
mvn clean install

# Or skip tests if you want faster build
mvn clean install -DskipTests
```

---

## Step 3: Start Services (In Order)

Open 5 separate terminal windows (or run in background):

### Terminal 1: Eureka Server
```bash
cd eureka-server
mvn spring-boot:run
# Access: http://localhost:8761
```

### Terminal 2: API Gateway
```bash
cd api-gateway
mvn spring-boot:run
# Access: http://localhost:8080
```

### Terminal 3: User Service
```bash
cd user-service
mvn spring-boot:run
# Access: http://localhost:8081
```

### Terminal 4: Booking Service
```bash
cd booking-service
mvn spring-boot:run
# Access: http://localhost:8082
```

### Terminal 5: Payment Service
```bash
cd payment-service
mvn spring-boot:run
# Access: http://localhost:8083
```

---

## Step 4: Test with Postman

### 1. Register a User
```
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "testuser",
  "email": "test@example.com",
  "role": "USER"
}
```

### 2. Make User Admin (Database)
```sql
UPDATE court_booking_users.users SET role = 'ADMIN' WHERE username = 'testuser';
```

### 3. Login (Get Token)
```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "testuser",
  "password": "password123"
}
```

### 4. Create Court (Admin Only)
```
POST http://localhost:8080/courts
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json

{
  "name": "Tennis Court 1",
  "sportType": "Tennis",
  "location": "Building A",
  "pricePerHour": 50.00
}
```

### 5. Get All Courts
```
GET http://localhost:8080/courts
Authorization: Bearer YOUR_TOKEN_HERE
```

### 6. Create Booking
```
POST http://localhost:8080/bookings
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json

{
  "userId": 1,
  "courtId": 1,
  "bookingDate": "2026-05-20",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "notes": "Morning practice"
}
```

### 7. Get My Bookings
```
GET http://localhost:8080/bookings/user/1
Authorization: Bearer YOUR_TOKEN_HERE
```

### 8. Process Payment
```
POST http://localhost:8080/api/payments/1/process
Authorization: Bearer YOUR_TOKEN_HERE
```

### 9. Cancel Booking
```
DELETE http://localhost:8080/bookings/1?userId=1
Authorization: Bearer YOUR_TOKEN_HERE
```

---

## Step 5: Run Tests with Coverage

```bash
# Run all tests with coverage
mvn test

# View JaCoCo coverage report
# Open in browser: target/site/jacoco/index.html
```

---

## Step 6: SonarQube Analysis

Start SonarQube (if not running):
```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest
```

Run analysis:
```bash
mvn clean verify sonar:sonar
```

Access: http://localhost:9000 (default: admin/admin)

---

## Service URLs Summary

| Service | URL | Swagger UI |
|---------|-----|------------|
| Eureka | http://localhost:8761 | N/A |
| API Gateway | http://localhost:8080 | N/A |
| User Service | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| Booking Service | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| Payment Service | http://localhost:8083 | http://localhost:8083/swagger-ui.html |

---

## Troubleshooting

### Issue: Connection refused to MySQL
**Solution:** Make sure MySQL is running and credentials match in `application.yml`

### Issue: Service not registering with Eureka
**Solution:** Wait 30 seconds for registration; check Eureka dashboard at http://localhost:8761

### Issue: JWT Token Expired
**Solution:** Login again to get new token (tokens expire after 24 hours by default)

### Issue: Port already in use
**Solution:** Kill the process using the port or change port in `application.yml`

### Issue: Tests failing
**Solution:** Make sure MySQL is running and databases are created

---

## Logs Location

After running services, logs are stored in:
```
CourtBook/
├── eureka-server/logs/
├── api-gateway/logs/
├── user-service/logs/
├── booking-service/logs/
└── payment-service/logs/
```