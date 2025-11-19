package org.mc.mcronalds.repository;

import org.mc.mcronalds.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory,Long> {
    Optional<MenuCategory> findByName(String name);
    List<MenuCategory> findByActiveTrue();
}
