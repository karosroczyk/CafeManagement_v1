package com.cafe.inventory.service;

import com.cafe.inventory.entity.InventoryItem;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    PaginatedResponse<InventoryItem> getAllInventoryItems(int page, int size, String[] sortBy, String[] direction);
    InventoryItem getInventoryItemById(Integer id);
    List<InventoryItem> getInventoryItemsByMenuItemIds(List<Integer> menuItemIds);
    public List<Boolean> areInventoryItemsByMenuItemIdsAvailable(List<Integer> menuItemIds);
    InventoryItem createInventoryItem(InventoryItem category);
    InventoryItem updateInventoryItem(Integer id, InventoryItem category);
    void deleteInventoryItem(Integer id);
    //InventoryItem reduceStockByMenuItemId(Integer id, Integer quantity);
    InventoryItem addStock(Integer id, Integer quantity);
}
