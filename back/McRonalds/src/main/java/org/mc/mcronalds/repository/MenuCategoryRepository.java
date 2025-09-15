package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory,Long> {
}
