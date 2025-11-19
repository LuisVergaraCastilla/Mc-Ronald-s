package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.Order;
import org.mc.mcronalds.model.OrderStatus;
import org.mc.mcronalds.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUser(User user);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
