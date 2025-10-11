package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.SaleDetail;
import org.mc.mcronalds.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleDetailRepository extends JpaRepository<SaleDetail,Long> {
    List<SaleDetail> findBySale(Sale sale);
}
