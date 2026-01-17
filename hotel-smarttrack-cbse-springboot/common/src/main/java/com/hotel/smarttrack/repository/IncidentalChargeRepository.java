package com.hotel.smarttrack.repository;

import com.hotel.smarttrack.entity.IncidentalCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data JPA Repository for IncidentalCharge entity.
 * Part of Base Library - provides data access for incidental charge operations.
 */
@Repository
public interface IncidentalChargeRepository extends JpaRepository<IncidentalCharge, Long> {

    /**
     * Find charges by stay ID.
     */
    @Query("SELECT ic FROM IncidentalCharge ic WHERE ic.stay.stayId = :stayId")
    List<IncidentalCharge> findByStayId(@Param("stayId") Long stayId);

    /**
     * Find charges by service type.
     */
    List<IncidentalCharge> findByServiceType(String serviceType);

    /**
     * Get total charges for a stay.
     */
    @Query("SELECT COALESCE(SUM(ic.amount), 0) FROM IncidentalCharge ic WHERE ic.stay.stayId = :stayId")
    BigDecimal getTotalChargesForStay(@Param("stayId") Long stayId);

    /**
     * Find charges by stay ID and service type.
     */
    @Query("SELECT ic FROM IncidentalCharge ic WHERE ic.stay.stayId = :stayId AND ic.serviceType = :serviceType")
    List<IncidentalCharge> findByStayIdAndServiceType(
            @Param("stayId") Long stayId,
            @Param("serviceType") String serviceType);
}
