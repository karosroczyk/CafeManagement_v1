package com.cafe.ordermanagement.service;

import com.cafe.ordermanagement.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {
    PaginatedResponse<Order> getAllOrders(int page, int size, String[] sortBy, String[] direction);
    Order getOrderById(Integer id);
    List<Map> getAvailableMenuItems(int page, int size, String[] sortBy, String[] direction);
    Order createOrder(Order menuItem);
    Order placeOrder(Order order);
    Order updateOrder(Integer id, Order menuItem);
    void deleteOrder(Integer id);
}
