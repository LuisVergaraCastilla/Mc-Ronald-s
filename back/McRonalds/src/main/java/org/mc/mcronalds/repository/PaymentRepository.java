package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
