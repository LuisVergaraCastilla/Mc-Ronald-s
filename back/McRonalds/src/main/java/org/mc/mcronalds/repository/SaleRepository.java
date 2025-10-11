package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.Sale;
import org.mc.mcronalds.model.Order;
import org.mc.mcronalds.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale,Long> {
    Optional<Sale> findByOrder(Order order);
    List<Sale> findByUser(User user);
    List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
