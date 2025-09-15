package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale,Long> {
}
