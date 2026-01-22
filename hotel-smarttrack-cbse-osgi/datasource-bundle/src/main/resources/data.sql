-- =============================================================================
-- Hotel SmartTrack OSGi - Seed Data
-- =============================================================================
-- Consistent test data matching Spring Boot version.
-- This file runs on startup to populate the in-memory H2 database.
-- =============================================================================

-- =============================================================================
-- 1. ROOM TYPES
-- =============================================================================
INSERT INTO room_types (room_type_id, type_name, description, max_occupancy, base_price, tax_rate) VALUES
(1, 'Standard', 'Standard room with basic amenities, 1 queen bed', 2, 100.00, 0.10),
(2, 'Deluxe', 'Deluxe room with premium amenities, 1 king bed, city view', 3, 200.00, 0.10),
(3, 'Suite', 'Luxury suite with separate living area and bedroom', 4, 350.00, 0.10),
(4, 'Family', 'Spacious room with 2 queen beds, ideal for families', 5, 280.00, 0.10),
(5, 'Penthouse', 'Top floor penthouse with panoramic city view', 6, 500.00, 0.12);

-- =============================================================================
-- 2. GUESTS
-- =============================================================================
INSERT INTO guests (guest_id, name, email, phone, identification_number, status, status_justification) VALUES
(1, 'John Smith', 'john.smith@email.com', '+1-555-0101', 'P12345678', 'ACTIVE', NULL),
(2, 'Emily Johnson', 'emily.j@email.com', '+1-555-0102', 'P23456789', 'ACTIVE', NULL),
(3, 'Michael Chen', 'mchen@email.com', '+1-555-0103', 'P34567890', 'ACTIVE', NULL),
(4, 'Sarah Williams', 'sarah.w@email.com', '+1-555-0104', 'P45678901', 'ACTIVE', NULL),
(5, 'David Brown', 'dbrown@email.com', '+1-555-0105', 'P56789012', 'ACTIVE', NULL),
(6, 'Lisa Anderson', 'lisa.a@email.com', '+1-555-0106', 'P67890123', 'INACTIVE', 'Account dormant for 2 years'),
(7, 'Robert Taylor', 'rtaylor@email.com', '+1-555-0107', 'P78901234', 'BLACKLISTED', 'Property damage incident'),
(8, 'Jennifer Martinez', 'jmartinez@email.com', '+1-555-0108', 'P89012345', 'ACTIVE', NULL),
(9, 'William Lee', 'wlee@email.com', '+1-555-0109', 'P90123456', 'ACTIVE', NULL),
(10, 'Amanda Garcia', 'agarcia@email.com', '+1-555-0110', 'P01234567', 'ACTIVE', NULL);

-- =============================================================================
-- 3. ROOMS
-- =============================================================================
-- Floor 1: Standard rooms
INSERT INTO rooms (room_id, room_number, floor_number, status, room_type_id) VALUES
(1, '101', 1, 'AVAILABLE', 1),
(2, '102', 1, 'AVAILABLE', 1),
(3, '103', 1, 'OCCUPIED', 1),
(4, '104', 1, 'UNDER_CLEANING', 1),
(5, '105', 1, 'AVAILABLE', 1);

-- Floor 2: Deluxe rooms
INSERT INTO rooms (room_id, room_number, floor_number, status, room_type_id) VALUES
(6, '201', 2, 'AVAILABLE', 2),
(7, '202', 2, 'OCCUPIED', 2),
(8, '203', 2, 'AVAILABLE', 2),
(9, '204', 2, 'OUT_OF_SERVICE', 2);

-- Floor 3: Suites
INSERT INTO rooms (room_id, room_number, floor_number, status, room_type_id) VALUES
(10, '301', 3, 'AVAILABLE', 3),
(11, '302', 3, 'OCCUPIED', 3),
(12, '303', 3, 'AVAILABLE', 3);

-- Floor 4: Family rooms
INSERT INTO rooms (room_id, room_number, floor_number, status, room_type_id) VALUES
(13, '401', 4, 'AVAILABLE', 4),
(14, '402', 4, 'AVAILABLE', 4);

-- Floor 5: Penthouse
INSERT INTO rooms (room_id, room_number, floor_number, status, room_type_id) VALUES
(15, '501', 5, 'AVAILABLE', 5);

-- =============================================================================
-- 4. RESERVATIONS
-- =============================================================================
-- Active reservations (future dates)
INSERT INTO reservations (reservation_id, guest_id, room_type_id, assigned_room_id, check_in_date, check_out_date, number_of_guests, status, special_requests) VALUES
(1, 1, 2, 6, '2026-01-25', '2026-01-28', 2, 'CONFIRMED', 'Late check-in after 10pm'),
(2, 2, 3, 10, '2026-01-26', '2026-01-30', 3, 'CONFIRMED', 'Anniversary celebration, champagne requested'),
(3, 3, 1, NULL, '2026-02-01', '2026-02-05', 1, 'RESERVED', NULL),
(4, 4, 4, 13, '2026-02-10', '2026-02-15', 4, 'CONFIRMED', 'Extra crib needed'),
(5, 5, 5, 15, '2026-02-20', '2026-02-25', 2, 'RESERVED', 'Honeymoon package');

-- Checked-in reservations (current stays)
INSERT INTO reservations (reservation_id, guest_id, room_type_id, assigned_room_id, check_in_date, check_out_date, number_of_guests, status, special_requests) VALUES
(6, 8, 1, 3, '2026-01-19', '2026-01-23', 2, 'CHECKED_IN', 'Quiet room preferred'),
(7, 9, 2, 7, '2026-01-18', '2026-01-22', 1, 'CHECKED_IN', NULL),
(8, 10, 3, 11, '2026-01-17', '2026-01-24', 2, 'CHECKED_IN', 'Business traveler, fast wifi');

-- Past reservations (history)
INSERT INTO reservations (reservation_id, guest_id, room_type_id, assigned_room_id, check_in_date, check_out_date, number_of_guests, status, special_requests) VALUES
(9, 1, 1, 1, '2026-01-05', '2026-01-08', 1, 'CHECKED_OUT', NULL),
(10, 2, 2, 6, '2026-01-10', '2026-01-12', 2, 'CHECKED_OUT', NULL),
(11, 3, 1, 2, '2025-12-20', '2025-12-25', 2, 'CANCELLED', 'Guest cancelled due to flight issues'),
(12, 4, 3, 10, '2025-12-28', '2025-12-31', 3, 'NO_SHOW', NULL);

-- =============================================================================
-- 5. STAYS
-- =============================================================================
-- Current active stays (CHECKED_IN)
INSERT INTO stays (stay_id, reservation_id, guest_id, room_id, check_in_time, check_out_time, status, key_card_number) VALUES
(1, 6, 8, 3, '2026-01-19 14:30:00', NULL, 'CHECKED_IN', 'KC-2026-0001'),
(2, 7, 9, 7, '2026-01-18 15:00:00', NULL, 'CHECKED_IN', 'KC-2026-0002'),
(3, 8, 10, 11, '2026-01-17 16:00:00', NULL, 'CHECKED_IN', 'KC-2026-0003');

-- Completed stays (CHECKED_OUT)
INSERT INTO stays (stay_id, reservation_id, guest_id, room_id, check_in_time, check_out_time, status, key_card_number) VALUES
(4, 9, 1, 1, '2026-01-05 14:00:00', '2026-01-08 11:00:00', 'CHECKED_OUT', 'KC-2026-0004'),
(5, 10, 2, 6, '2026-01-10 15:30:00', '2026-01-12 10:30:00', 'CHECKED_OUT', 'KC-2026-0005');

-- Walk-in stay (no reservation linked)
INSERT INTO stays (stay_id, reservation_id, guest_id, room_id, check_in_time, check_out_time, status, key_card_number) VALUES
(6, NULL, 5, 2, '2026-01-20 18:00:00', NULL, 'CHECKED_IN', 'KC-2026-0006');

-- =============================================================================
-- 6. INCIDENTAL CHARGES
-- =============================================================================
-- Charges for current stays
INSERT INTO incidental_charges (charge_id, stay_id, service_type, description, amount, charge_time) VALUES
(1, 1, 'Room Service', 'Breakfast in room - Continental', 25.00, '2026-01-20 08:30:00'),
(2, 1, 'Minibar', 'Snacks and beverages', 35.50, '2026-01-20 22:00:00'),
(3, 2, 'Laundry', 'Express laundry service - 5 items', 45.00, '2026-01-19 10:00:00'),
(4, 2, 'Spa', 'Full body massage - 60 mins', 120.00, '2026-01-19 16:00:00'),
(5, 3, 'Room Service', 'Dinner - Steak and wine', 85.00, '2026-01-18 20:00:00'),
(6, 3, 'F&B', 'Bar tab - Cocktails', 65.00, '2026-01-18 23:00:00'),
(7, 3, 'Business Center', 'Printing and courier service', 30.00, '2026-01-19 09:00:00');

-- Charges for completed stays
INSERT INTO incidental_charges (charge_id, stay_id, service_type, description, amount, charge_time) VALUES
(8, 4, 'Room Service', 'Room service dinner', 55.00, '2026-01-06 19:00:00'),
(9, 4, 'Minibar', 'Minibar consumption', 28.00, '2026-01-07 21:00:00'),
(10, 5, 'Spa', 'Couples massage package', 200.00, '2026-01-11 14:00:00');

-- =============================================================================
-- 7. INVOICES
-- =============================================================================
INSERT INTO invoices (invoice_id, stay_id, guest_id, room_charges, incidental_charges, taxes, discounts, total_amount, amount_paid, outstanding_balance, status, issued_time) VALUES
(1, 4, 1, 300.00, 83.00, 38.30, 0.00, 421.30, 0.00, 421.30, 'ISSUED', '2026-01-08 11:00:00'),
(2, 5, 2, 400.00, 200.00, 60.00, 50.00, 610.00, 0.00, 610.00, 'ISSUED', '2026-01-12 10:30:00'),
(3, 3, 10, 1050.00, 180.00, 123.00, 0.00, 1353.00, 0.00, 1353.00, 'ISSUED', '2026-01-20 12:00:00');

-- =============================================================================
-- SUMMARY
-- =============================================================================
-- room_types:          5 types
-- guests:             10 guests
-- rooms:              15 rooms
-- reservations:       12 reservations
-- stays:               6 stays
-- incidental_charges: 10 charges
-- invoices:            3 invoices
-- =============================================================================
