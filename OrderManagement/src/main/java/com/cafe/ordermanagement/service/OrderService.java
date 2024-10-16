package com.cafe.ordermanagement.service;

import com.cafe.ordermanagement.entity.Order;

public interface OrderService {
    PaginatedResponse<Order> getAllOrders(int page, int size, String[] sortBy, String[] direction);
    Order getOrderById(Integer id);
    Order createOrder(Order menuItem);
    Order placeOrder(Order order);
    Order updateOrder(Integer id, Order menuItem);
    void deleteOrder(Integer id);
}
