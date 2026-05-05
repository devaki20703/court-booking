-- Booking Service Database Schema
-- Database: court_booking_db

CREATE DATABASE IF NOT EXISTS court_booking_db;
USE court_booking_db;

CREATE TABLE IF NOT EXISTS courts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sport_type VARCHAR(50) NOT NULL,
    location VARCHAR(200) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    court_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE CASCADE
);

CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_court_id ON bookings(court_id);
CREATE INDEX idx_bookings_date ON bookings(booking_date);

-- Insert sample courts
INSERT INTO courts (name, sport_type, location, available, description) VALUES
('Court Alpha', 'Tennis', 'Ground Floor - Court 1', TRUE, 'Professional tennis court with hard surface'),
('Court Beta', 'Badminton', 'Ground Floor - Court 2', TRUE, 'Badminton court with carpet surface'),
('Court Gamma', 'Basketball', 'First Floor - Court 1', TRUE, 'Full size basketball court'),
('Court Delta', 'Volleyball', 'First Floor - Court 2', TRUE, 'Indoor volleyball court'),
('Court Epsilon', 'Tennis', 'Second Floor - Court 3', TRUE, 'Clay tennis court');

-- Insert sample bookings
INSERT INTO bookings (user_id, court_id, booking_date, start_time, end_time, status, notes) VALUES
(1, 1, '2026-04-20', '09:00:00', '10:00:00', 'CONFIRMED', 'Morning practice'),
(1, 2, '2026-04-20', '11:00:00', '12:00:00', 'CONFIRMED', 'Training session'),
(2, 3, '2026-04-21', '14:00:00', '15:30:00', 'CONFIRMED', 'Match practice');