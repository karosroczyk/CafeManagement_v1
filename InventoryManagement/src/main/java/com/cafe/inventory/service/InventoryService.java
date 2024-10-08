package com.cafe.inventory.service;

import com.cafe.inventory.entity.InventoryItem;

public interface InventoryService {
    PaginatedResponse<InventoryItem> getAllInventoryItems(int page, int size, String[] sortBy, String[] direction);
    InventoryItem getInventoryItemById(Integer id);
    InventoryItem createInventoryItem(InventoryItem category);
    InventoryItem updateInventoryItem(Integer id, InventoryItem category);
    void deleteInventoryItem(Integer id);
    InventoryItem reduceStock(Integer id, Integer quantity);
    InventoryItem addStock(Integer id, Integer quantity);
}
