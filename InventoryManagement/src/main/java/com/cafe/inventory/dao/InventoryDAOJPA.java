package com.cafe.inventory.dao;

import com.cafe.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryDAOJPA extends JpaRepository<InventoryItem, Integer> {
}
