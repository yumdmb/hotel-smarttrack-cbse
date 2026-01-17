package com.hotel.smarttrack.repository;

import com.hotel.smarttrack.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for RoomType entity.
 * Part of Base Library - provides data access for room type operations.
 */
@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    /**
     * Find room type by name (case-insensitive).
     */
    Optional<RoomType> findByTypeNameIgnoreCase(String typeName);

    /**
     * Check if a room type name already exists (case-insensitive).
     */
    boolean existsByTypeNameIgnoreCase(String typeName);
}
