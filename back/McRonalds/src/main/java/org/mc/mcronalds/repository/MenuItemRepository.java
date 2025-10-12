package org.mc.mcronalds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.mc.mcronalds.model.MenuItem;
import org.mc.mcronalds.model.MenuCategory;
import java.util.List;


public interface MenuItemRepository extends JpaRepository<MenuItem,Long> {
    List<MenuItem> findByCategory(MenuCategory category);
}
