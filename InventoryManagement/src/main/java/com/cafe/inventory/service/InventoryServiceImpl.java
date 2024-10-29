package com.cafe.inventory.service;

import com.cafe.inventory.dao.InventoryDAOJPA;
import com.cafe.inventory.entity.InventoryItem;
import com.cafe.inventory.exception.DatabaseUniqueValidationException;
import com.cafe.inventory.exception.InvalidInputException;
import com.cafe.inventory.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryDAOJPA inventoryDAOJPA;
    private final WebClient webClient;
    @Value("${menu.service.url}")
    private String menuServiceUrl;

    public InventoryServiceImpl(InventoryDAOJPA inventoryDAOJPA, WebClient webClient) {
        this.inventoryDAOJPA = inventoryDAOJPA;
        this.webClient = webClient;
    }
    @Override
    public PaginatedResponse<InventoryItem> getAllInventoryItems(int page, int size, String[] sortBy, String[] direction) {
        List<Sort.Order> orders = IntStream.range(0, sortBy.length)
                .mapToObj(i -> new Sort.Order(Sort.Direction.fromString(direction[i]), sortBy[i]))
                .toList();

        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<InventoryItem> categoryPage = this.inventoryDAOJPA.findAll(pageable);

        return new PaginatedResponse<>(
                categoryPage.getContent(),
                categoryPage.getNumber(),
                categoryPage.getTotalPages(),
                categoryPage.getTotalElements(),
                categoryPage.getSize());
    }

    @Override
    public InventoryItem getInventoryItemById(Integer id) {
        return this.inventoryDAOJPA.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem with id: " + id + " not found."));
    }
    @Override
    public List<InventoryItem> getInventoryItemsByMenuItemIds(List<Integer> menuItemIds) {
        List<Optional<InventoryItem>> optionalinventoryItems = menuItemIds.stream()
                .map(menuItemId -> this.inventoryDAOJPA.findByMenuItemId(menuItemId)).toList();

        Boolean check = optionalinventoryItems.stream().anyMatch(id -> id.isEmpty());
        if(check) throw new ResourceNotFoundException("InventoryItem with id not found.");

        return optionalinventoryItems.stream().map(Optional::get).toList();
    }

    @Override
    public List<Boolean> areInventoryItemsByMenuItemIdsAvailable(List<Integer> menuItemIds, List<Integer> quantitiesOfMenuItems) {
        List<InventoryItem> inventoryItemsByMenuItemsIds =
                this.getInventoryItemsByMenuItemIds(menuItemIds);
        return inventoryItemsByMenuItemsIds.stream().map(inventoryItem ->
            (inventoryItem.isAvailable() &&
                    inventoryItem.getStockLevel() >= quantitiesOfMenuItems.get(menuItemIds.indexOf(inventoryItem.getMenuItemId())))).toList();
    }

    @Override
    @Transactional
    public InventoryItem createInventoryItem(InventoryItem inventoryItem){
        try {
            return this.inventoryDAOJPA.save(inventoryItem);
        }catch(DataIntegrityViolationException e){
            throw new DatabaseUniqueValidationException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public InventoryItem updateInventoryItem(Integer id, InventoryItem inventoryItem){
        InventoryItem foundInventoryItem = getInventoryItemById(id);

        foundInventoryItem.setMenuItemId(inventoryItem.getMenuItemId());
        foundInventoryItem.setStockLevel(inventoryItem.getStockLevel());
        foundInventoryItem.setAvailable(inventoryItem.isAvailable());

        try {
            return this.inventoryDAOJPA.save(foundInventoryItem);
        }catch(DataIntegrityViolationException e){
            throw new DatabaseUniqueValidationException(e.getRootCause().getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteInventoryItem(Integer id) {
        getInventoryItemById(id);
        try {
            this.inventoryDAOJPA.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseUniqueValidationException(e.getRootCause().getMessage());
        }
    }

    @Override
    @Transactional
    public List<InventoryItem> reduceStockByMenuItemId(List<Integer> menuItemIds, List<Integer> quantitiesOfMenuItems){
        List<InventoryItem> savedInventoryItems = new ArrayList<>();
        List<InventoryItem> foundInventoryItems = this.getInventoryItemsByMenuItemIds(menuItemIds);
        foundInventoryItems.stream().forEach(foundInventoryItem -> {
            Integer updatedStock =
                    foundInventoryItem.getStockLevel() - quantitiesOfMenuItems.get(foundInventoryItems.indexOf(foundInventoryItem));

            if (updatedStock < 0)
                throw new InvalidInputException("Not enough stock to reduce.");

            if (updatedStock == 0)
                foundInventoryItem.setAvailable(false);

            foundInventoryItem.setStockLevel(updatedStock);
            savedInventoryItems.add(this.inventoryDAOJPA.save(foundInventoryItem));
        });
        return savedInventoryItems;
    }

//    @Override
//    @Transactional
//    public InventoryItem reduceStockByMenuItemId(Integer menuItemId, Integer quantity){
//        InventoryItem foundInventoryItem = this.getInventoryItemByMenuItemId(menuItemId);
//        Integer updatedStock = foundInventoryItem.getStockLevel() - quantity;
//
//        if (updatedStock < 0)
//            throw new InvalidInputException("Not enough stock to reduce.");
//
//        if (updatedStock == 0) {
//            foundInventoryItem.setAvailable(false);
//        }
//
//        foundInventoryItem.setStockLevel(updatedStock);
//        return this.inventoryDAOJPA.save(foundInventoryItem);
//    }

    @Override
    @Transactional
    public InventoryItem addStock(Integer id, Integer quantity){
        InventoryItem foundInventoryItem = this.getInventoryItemById(id);
        Integer updatedStock = foundInventoryItem.getStockLevel() + quantity;

        if (updatedStock > 0)
            foundInventoryItem.setAvailable(true);

        foundInventoryItem.setStockLevel(updatedStock);
        return this.inventoryDAOJPA.save(foundInventoryItem);
    }
}
