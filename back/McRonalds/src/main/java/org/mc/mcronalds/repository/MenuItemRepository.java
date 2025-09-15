package org.mc.mcronalds.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.*;

public interface MenuItemRepository extends JpaRepository<MenuItem,Long> {
}
