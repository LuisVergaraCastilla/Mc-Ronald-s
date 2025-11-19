package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.Payment;
import org.mc.mcronalds.model.PaymentStatus;
import org.mc.mcronalds.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByOrder_IdOrder(Long idOrder);
    List<Payment> findByOrder(Order order);
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
}
