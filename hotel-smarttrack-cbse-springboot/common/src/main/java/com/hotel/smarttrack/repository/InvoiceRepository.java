package com.hotel.smarttrack.repository;

import com.hotel.smarttrack.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByStay_StayId(Long stayId);
}
