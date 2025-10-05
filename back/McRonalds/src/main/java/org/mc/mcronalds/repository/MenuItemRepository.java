package org.mc.mcronalds.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.mc.mcronalds.model.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem,Long> {
}
