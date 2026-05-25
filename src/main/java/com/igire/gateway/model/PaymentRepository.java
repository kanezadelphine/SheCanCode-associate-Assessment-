package com.igire.gateway.repository;

import com.igire.gateway.model.CachedPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<CachedPayment, String> {
}