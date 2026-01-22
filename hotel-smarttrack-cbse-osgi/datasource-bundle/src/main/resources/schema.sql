-- =============================================================================
-- Hotel SmartTrack OSGi - Database Schema
-- =============================================================================
-- H2 Database schema for all entities.
-- This file runs on bundle activation to create tables if they don't exist.
-- =============================================================================

-- =============================================================================
-- 1. ROOM TYPES (referenced by rooms)
-- =============================================================================
CREATE TABLE IF NOT EXISTS room_types (
    room_type_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    max_occupancy INT NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    tax_rate DECIMAL(5,4) DEFAULT 0.10
);

-- =============================================================================
-- 2. GUESTS
-- =============================================================================
CREATE TABLE IF NOT EXISTS guests (
    guest_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(200),
    phone VARCHAR(50),
    identification_number VARCHAR(100),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    status_justification VARCHAR(500)
);

-- =============================================================================
-- 3. ROOMS (references room_types)
-- =============================================================================
CREATE TABLE IF NOT EXISTS rooms (
    room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL,
    floor_number INT,
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    room_type_id BIGINT,
    FOREIGN KEY (room_type_id) REFERENCES room_types(room_type_id)
);

-- =============================================================================
-- 4. RESERVATIONS (references guests, room_types, rooms)
-- =============================================================================
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guest_id BIGINT NOT NULL,
    room_type_id BIGINT,
    assigned_room_id BIGINT,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_guests INT DEFAULT 1,
    status VARCHAR(50) DEFAULT 'RESERVED',
    special_requests VARCHAR(1000),
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(room_type_id),
    FOREIGN KEY (assigned_room_id) REFERENCES rooms(room_id)
);

-- =============================================================================
-- 5. STAYS (references reservations, guests, rooms)
-- =============================================================================
CREATE TABLE IF NOT EXISTS stays (
    stay_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT,
    guest_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    status VARCHAR(50) DEFAULT 'CHECKED_IN',
    key_card_number VARCHAR(50),
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id),
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

-- =============================================================================
-- 6. INCIDENTAL CHARGES (references stays)
-- =============================================================================
CREATE TABLE IF NOT EXISTS incidental_charges (
    charge_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stay_id BIGINT NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(10,2) NOT NULL,
    charge_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stay_id) REFERENCES stays(stay_id)
);

-- =============================================================================
-- 7. INVOICES (references stays, reservations)
-- =============================================================================
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stay_id BIGINT,
    reservation_id BIGINT,
    amount DECIMAL(10,2) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'UNPAID',
    issued_at TIMESTAMP,
    FOREIGN KEY (stay_id) REFERENCES stays(stay_id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

-- =============================================================================
-- 8. PAYMENTS (references invoices)
-- =============================================================================
CREATE TABLE IF NOT EXISTS payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50),
    transaction_reference VARCHAR(100),
    payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'COMPLETED',
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id)
);
