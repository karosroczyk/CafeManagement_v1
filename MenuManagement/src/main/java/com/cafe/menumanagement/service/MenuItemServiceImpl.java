package com.cafe.menumanagement.service;

import com.cafe.menumanagement.dao.MenuItemDAOJPA;
import com.cafe.menumanagement.entity.MenuItem;
import com.cafe.menumanagement.exception.DatabaseUniqueValidationException;
import com.cafe.menumanagement.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class MenuItemServiceImpl implements MenuItemService{
    private final MenuItemDAOJPA menuItemDAOJPA;
    public MenuItemServiceImpl(MenuItemDAOJPA menuItemDAOJPA){
        this.menuItemDAOJPA = menuItemDAOJPA;
    }
    @Override
    public PaginatedResponse<MenuItem> getAllMenuItems(int page, int size, String[] sortBy, String[] direction) {
        List<Sort.Order> orders = IntStream.range(0, sortBy.length)
                .mapToObj(i -> new Sort.Order(Sort.Direction.fromString(direction[i]), sortBy[i]))
                .toList();
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<MenuItem> menuItemPage = this.menuItemDAOJPA.findAll(pageable);

        return new PaginatedResponse<>(
                menuItemPage.getContent(),
                menuItemPage.getNumber(),
                menuItemPage.getTotalPages(),
                menuItemPage.getTotalElements(),
                menuItemPage.getSize());
    }

    @Override
    public PaginatedResponse<MenuItem> getMenuItemsByCategoryName(String categoryName, int page, int size, String[] sortBy, String[] direction){
        List<Sort.Order> orders = IntStream.range(0, sortBy.length)
                .mapToObj(i -> new Sort.Order(Sort.Direction.fromString(direction[i]), sortBy[i]))
                .toList();
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<MenuItem> menuItemPage = this.menuItemDAOJPA.findByCategoryName(categoryName, pageable);
        return new PaginatedResponse<>(
                menuItemPage.getContent(),
                menuItemPage.getNumber(),
                menuItemPage.getTotalPages(),
                menuItemPage.getTotalElements(),
                menuItemPage.getSize());
    }
    @Override
    public MenuItem getMenuItemById(Integer id) {
        return this.menuItemDAOJPA.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem with id: " + id + " not found."));
    }

    @Override
    @Transactional
    public MenuItem createMenuItem(MenuItem menuItem){
        try {
            return this.menuItemDAOJPA.save(menuItem);
        }catch(DataIntegrityViolationException e){
            throw new DatabaseUniqueValidationException(e.getRootCause().getMessage());
        }
    }
    @Override
    @Transactional
    public MenuItem updateMenuItem(Integer id, MenuItem menuItem){
        MenuItem foundMenuItem = getMenuItemById(id);

        foundMenuItem.setName(menuItem.getName());
        foundMenuItem.setDescription(menuItem.getDescription());
        foundMenuItem.setPrice(menuItem.getPrice());
        foundMenuItem.setCategoryId(menuItem.getCategoryId());
        foundMenuItem.setAvailable(menuItem.isAvailable());
        foundMenuItem.setImage(menuItem.getImage());

        try {
            return this.menuItemDAOJPA.save(foundMenuItem);
        }catch(DataIntegrityViolationException e){
            throw new DatabaseUniqueValidationException(e.getRootCause().getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteMenuItem(Integer id) {
        getMenuItemById(id);
        try {
            this.menuItemDAOJPA.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseUniqueValidationException(e.getRootCause().getMessage());
        }
    }
}
