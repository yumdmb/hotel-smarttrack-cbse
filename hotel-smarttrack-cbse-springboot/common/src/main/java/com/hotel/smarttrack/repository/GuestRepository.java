package com.hotel.smarttrack.repository;

import com.hotel.smarttrack.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Guest entity.
 * Part of Base Library - provides data access for guest-related operations.
 */
@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    /**
     * Find guest by email.
     */
    Optional<Guest> findByEmail(String email);

    /**
     * Find guest by identification number.
     */
    Optional<Guest> findByIdentificationNumber(String identificationNumber);

    /**
     * Find guests by status.
     */
    List<Guest> findByStatus(String status);

    /**
     * Search guests by name, email, phone, or identification number.
     */
    @Query("SELECT g FROM Guest g WHERE " +
            "LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(g.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "g.phone LIKE CONCAT('%', :searchTerm, '%') OR " +
            "g.identificationNumber LIKE CONCAT('%', :searchTerm, '%')")
    List<Guest> searchGuests(@Param("searchTerm") String searchTerm);

    /**
     * Find all active guests.
     */
    @Query("SELECT g FROM Guest g WHERE g.status = 'ACTIVE'")
    List<Guest> findAllActive();

    /**
     * Check if email already exists.
     */
    boolean existsByEmail(String email);

    /**
     * Check if identification number already exists.
     */
    boolean existsByIdentificationNumber(String identificationNumber);
}
