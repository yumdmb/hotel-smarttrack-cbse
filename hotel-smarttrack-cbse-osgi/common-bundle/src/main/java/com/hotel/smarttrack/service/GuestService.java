package com.hotel.smarttrack.service;

import com.hotel.smarttrack.entity.Guest;
import java.util.List;
import java.util.Optional;

/**
 * GuestService interface - exposes Guest Management functionality.
 * Part of Base Library (CBSE Rule 5) - interface in common library.
 * 
 * <p>
 * Implemented by GuestManager in guest-management-bundle.
 * </p>
 * 
 * <p>
 * Handles UC1-UC4:
 * </p>
 * <ul>
 * <li>UC1: Manage Guest Records</li>
 * <li>UC2: Search Guest Profiles</li>
 * <li>UC3: Retrieve Guest Stay & Reservation History</li>
 * <li>UC4: Manage Guest Status</li>
 * </ul>
 * 
 * @author Ma Wenting
 */
public interface GuestService {

    // ============ Guest CRUD Operations (UC1) ============

    /**
     * Create a new guest profile.
     * 
     * @param name                 Guest name
     * @param email                Guest email
     * @param phone                Guest phone number
     * @param identificationNumber Guest ID number
     * @return Created guest with generated ID
     */
    Guest createGuest(String name, String email, String phone, String identificationNumber);

    /**
     * Update an existing guest profile.
     * 
     * @param guest Guest with updated information
     * @return Updated guest
     * @throws IllegalArgumentException if guest not found
     */
    Guest updateGuest(Guest guest);

    /**
     * Retrieve a guest by ID.
     * 
     * @param guestId Guest ID
     * @return Guest if found
     */
    Optional<Guest> getGuestById(Long guestId);

    /**
     * Get all guests in the system.
     * 
     * @return List of all guests
     */
    List<Guest> getAllGuests();

    // ============ Search Operations (UC2) ============

    /**
     * Search guests by criteria (name, email, phone, or ID number).
     * 
     * @param searchTerm Search term to match against guest fields
     * @return List of matching guests
     */
    List<Guest> searchGuests(String searchTerm);

    /**
     * Find guest by email address.
     * 
     * @param email Email address
     * @return Guest if found
     */
    Optional<Guest> findByEmail(String email);

    /**
     * Find guest by phone number.
     * 
     * @param phone Phone number
     * @return Guest if found
     */
    Optional<Guest> findByPhone(String phone);

    /**
     * Find guest by identification number.
     * 
     * @param identificationNumber ID number
     * @return Guest if found
     */
    Optional<Guest> findByIdentificationNumber(String identificationNumber);

    // ============ Status Management (UC4) ============

    /**
     * Deactivate a guest profile.
     * 
     * @param guestId       Guest ID
     * @param justification Reason for deactivation
     * @throws IllegalArgumentException if guest not found
     */
    void deactivateGuest(Long guestId, String justification);

    /**
     * Mark a guest as blacklisted.
     * 
     * @param guestId       Guest ID
     * @param justification Reason for blacklisting
     * @throws IllegalArgumentException if guest not found
     */
    void blacklistGuest(Long guestId, String justification);

    /**
     * Reactivate a guest profile (set status back to Active).
     * 
     * @param guestId Guest ID
     * @throws IllegalArgumentException if guest not found
     */
    void reactivateGuest(Long guestId);

    /**
     * Get guests by status.
     * 
     * @param status Status to filter by (Active, Inactive, Blacklisted)
     * @return List of guests with the specified status
     */
    List<Guest> getGuestsByStatus(String status);
}
