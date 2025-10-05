package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByOrder_IdOrder(Long idOrder);
}
