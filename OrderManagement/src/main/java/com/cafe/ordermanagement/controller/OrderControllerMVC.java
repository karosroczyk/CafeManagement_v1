package com.cafe.ordermanagement.controller;

import com.cafe.ordermanagement.dto.MenuItem;
import com.cafe.ordermanagement.entity.Order;
import com.cafe.ordermanagement.entity.OrderMenuItemId;
import com.cafe.ordermanagement.entity.OrderMenuItemIdKey;
import com.cafe.ordermanagement.exception.InvalidInputException;
import com.cafe.ordermanagement.service.OrderService;
import com.cafe.ordermanagement.service.PaginatedResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderControllerMVC {

    private final OrderService orderService;
    @Autowired
    private WebClient webClient;
    @Value("${menu.service.url}")
    private String menuServiceUrl;
    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public OrderControllerMVC(OrderService orderService, WebClient webClient) {
        this.webClient = webClient;
        this.orderService = orderService;
    }

    @GetMapping
    public String getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "customerId") String[] sortBy,
            @RequestParam(defaultValue = "asc") String[] direction,
            Model model) {
        if (page < 0 || size <= 0 || sortBy.length != direction.length) {
            throw new InvalidInputException("Invalid page: " + page + ", size: " + size + " provided.");
        }

        PaginatedResponse<Order> orders = this.orderService.getAllOrders(page, size, sortBy, direction);
        model.addAttribute("orders", orders.getData());
        model.addAttribute("currentPage", orders.getCurrentPage());
        model.addAttribute("totalPages", orders.getTotalPages());
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String getOrderById(@PathVariable Integer id, Model model) {
        if (id < 0) {
            throw new InvalidInputException("Invalid id: " + id + " provided.");
        }

        Order order = this.orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "orders/detail"; // View for showing order details
    }

    @GetMapping("/menuitems")
    public String showAllMenuItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size,
            @RequestParam(defaultValue = "name") String[] sortBy,
            @RequestParam(defaultValue = "asc") String[] direction,
            Model model) {
        if (page < 0 || size <= 0 || sortBy.length != direction.length) {
            throw new InvalidInputException("Invalid page: " + page + ", size: " + size + " provided.");
        }

        PaginatedResponse<MenuItem> menuItems = orderService.getAvailableMenuItems(page, size, sortBy, direction);
        model.addAttribute("menuItems", menuItems.getData());
        model.addAttribute("currentPage", menuItems.getCurrentPage());
        model.addAttribute("totalPages", menuItems.getTotalPages());
        return "orders/menuitems";
    }

    @GetMapping("/new")
    public String showCreateOrderForm(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(defaultValue = "name") String[] sortBy,
                                  @RequestParam(defaultValue = "asc") String[] direction,
                                  Model model) {
        if (page < 0 || size <= 0 || sortBy.length != direction.length) {
            throw new InvalidInputException("Invalid page: " + page + ", size: " + size + " provided.");
        }

        model.addAttribute("order", new Order()); // Create a blank form with an empty Order object
        PaginatedResponse<MenuItem> menuItems = orderService.getAvailableMenuItems(page, size, sortBy, direction);
        model.addAttribute("menuItems", menuItems.getData());
        model.addAttribute("currentPage", menuItems.getCurrentPage());
        model.addAttribute("totalPages", menuItems.getTotalPages());
        return "orders/create"; // View for the order creation form
    }

    @PostMapping
    public String createOrder(@Valid @ModelAttribute("order") Order order,
                              @RequestParam(required = false) List<Integer> menuItemIds, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "orders/create"; // Return the same form with validation errors
        }

        Order createdOrder = this.orderService.placeOrder(menuItemIds, order);
        return "redirect:/orders/" + createdOrder.getId(); // Redirect to the newly created order's detail page
    }

    @GetMapping("/edit/{id}")
    public String showUpdateOrderForm(@PathVariable Integer id, Model model) {
        if (id < 0) {
            throw new InvalidInputException("Invalid id: " + id + " provided.");
        }

        Order order = this.orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "orders/edit"; // View for the order update form
    }

    @PostMapping("/update/{id}")
    public String updateOrder(
            @PathVariable Integer id,
            @Valid @ModelAttribute("order") Order order,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "orders/edit"; // Return the same form with validation errors
        }

        if (id < 0) {
            throw new InvalidInputException("Invalid id: " + id + " provided.");
        }

        Order updatedOrder = this.orderService.updateOrder(id, order);
        return "redirect:/orders/" + updatedOrder.getId(); // Redirect to the updated order's detail page
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Integer id) {
        if (id < 0) {
            throw new InvalidInputException("Invalid id: " + id + " provided.");
        }

        this.orderService.deleteOrder(id);
        return "redirect:/orders"; // Redirect to the order list after deletion
    }
}
