package com.hotel.smarttrack.billing;

import com.hotel.smarttrack.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
