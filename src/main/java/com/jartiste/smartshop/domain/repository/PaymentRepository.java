package com.jartiste.smartshop.domain.repository;

import com.jartiste.smartshop.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
