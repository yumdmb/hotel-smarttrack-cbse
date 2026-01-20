-- =============================================================================
-- Hotel SmartTrack - Sample Seed Data
-- =============================================================================
-- This file is OPTIONAL. It provides initial data for development/testing.
-- 
-- IMPORTANT: Tables are AUTO-CREATED by Hibernate from @Entity classes!
-- You don't need to maintain schema.sql - just add entities to common module.
--
-- HOW IT WORKS:
-- 1. Hibernate creates tables from entities (ddl-auto=update)
-- 2. This data.sql runs AFTER tables are created
-- 3. Sample data is inserted for testing
--
-- TO DISABLE: Set spring.sql.init.mode=never in application.properties
-- =============================================================================

-- =============================================================================
-- GUESTS (Sample Data)
-- =============================================================================
INSERT INTO guests (name, email, phone, identification_number, status, status_justification) VALUES
('John Smith', 'john.smith@email.com', '+60-12-345-6789', 'A12345678', 'Active', NULL),
('Sarah Johnson', 'sarah.johnson@email.com', '+60-12-456-7890', 'B23456789', 'Active', NULL),
('Michael Chen', 'michael.chen@email.com', '+60-12-567-8901', 'C34567890', 'Active', NULL),
('Emily Wong', 'emily.wong@email.com', '+60-12-678-9012', 'D45678901', 'Active', NULL);

-- =============================================================================
-- ROOM TYPES (Sample Data)
-- =============================================================================
INSERT INTO room_types (type_name, description, max_occupancy, base_price, tax_rate) VALUES
('Standard', 'Comfortable standard room with essential amenities', 2, 150.00, 0.10),
('Deluxe', 'Spacious deluxe room with city view', 2, 250.00, 0.10),
('Suite', 'Luxurious suite with separate living area', 4, 450.00, 0.10),
('Family', 'Large family room with multiple beds', 5, 350.00, 0.10);

-- =============================================================================
-- ROOMS (Sample Data)
-- =============================================================================
INSERT INTO rooms (room_number, floor_number, room_type_id, status) VALUES
('101', 1, 1, 'Available'),
('102', 1, 1, 'Available'),
('103', 1, 1, 'Available'),
('201', 2, 2, 'Available'),
('202', 2, 2, 'Available'),
('301', 3, 3, 'Available'),
('401', 4, 4, 'Available');

-- =============================================================================
-- RESERVATIONS (Sample Data)
-- =============================================================================
INSERT INTO reservations (guest_id, room_type_id, room_id, check_in_date, check_out_date, number_of_guests, status, special_requests) VALUES
(1, 2, 4, '2026-01-20', '2026-01-25', 2, 'Confirmed', 'Late checkout if possible'),
(2, 3, 6, '2026-01-22', '2026-01-26', 3, 'Reserved', 'Extra pillows');

-- =============================================================================
-- STAYS (Sample Data) - For active check-ins
-- =============================================================================
INSERT INTO stays (reservation_id, guest_id, room_id, check_in_time, check_out_time, status, key_card_number) VALUES
(1, 1, 4, '2026-01-20 14:30:00', NULL, 'Active', 'KC-2026-0001');
