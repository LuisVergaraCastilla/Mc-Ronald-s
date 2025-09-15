package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
