package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.OrderDetail;
import org.mc.mcronalds.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail,Long> {
    List<OrderDetail> findByOrder(Order order);
}
