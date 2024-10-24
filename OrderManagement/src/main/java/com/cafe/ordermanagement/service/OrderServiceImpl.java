package com.cafe.ordermanagement.service;

import com.cafe.ordermanagement.entity.OrderMenuItemId;
import com.cafe.ordermanagement.entity.OrderMenuItemIdKey;
import com.cafe.ordermanagement.exception.DatabaseUniqueValidationException;
import com.cafe.ordermanagement.exception.ResourceNotFoundException;
import com.cafe.ordermanagement.dao.OrderDAOJPA;
import com.cafe.ordermanagement.entity.Order;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    private final OrderDAOJPA orderDAOJPA;
    @Autowired
    private WebClient webClient;
    @Value("${menu.service.url}")
    private String menuServiceUrl;
    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public OrderServiceImpl(OrderDAOJPA orderDAOJPA, WebClient webClient){
        this.orderDAOJPA = orderDAOJPA;
        this.webClient = webClient;
    }

    public List<Map> getAvailableMenuItems(int page, int size, String[] sortBy, String[] direction) {
        List<Sort.Order> orders = IntStream.range(0, sortBy.length)
                .mapToObj(i -> new Sort.Order(Sort.Direction.fromString(direction[i]), sortBy[i]))
                .toList();
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));

        var menuItems = webClient
                .get()
                .uri(menuServiceUrl + "/api/menuitems")
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    System.err.println("Client error: " + response.statusCode());
                    return Mono.error(new RuntimeException("Client error occurred"));
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    System.err.println("Server error: " + response.statusCode());
                    return Mono.error(new RuntimeException("Server error occurred"));
                })
                .bodyToFlux(Map.class)
                .collectList()
                .block();

        return menuItems;
    }

    //getAllAvailableItems
    //wyswietla liste tylko dostepnych menuitems i pozniej nie musimy juz sprawdzac dostepnosci
    //nadal trzeba sprawdzic ilosc

    //wyswietla liste wszystkich menuitems
    //uzytkownik wybiera co chce i klika placeOrder
    //sprawdzmy czy wszytskie sa dostepne
    //TODO: add possibility to place order for many of the same menuitem
    @Override
    @Transactional
    public Order placeOrder(Order order) {
        String urlMenuService = menuServiceUrl + "/api/menuitems";
        String urlInventoryService = inventoryServiceUrl + "/api/inventory";

        Order placedOrder = createOrder(order);

        OrderMenuItemId menuItem1 = new OrderMenuItemId(new OrderMenuItemIdKey(placedOrder.getId(), 1));
        placedOrder.addMenuItem(menuItem1);

        OrderMenuItemId menuItem2 = new OrderMenuItemId(new OrderMenuItemIdKey(placedOrder.getId(), 2));
        placedOrder.addMenuItem(menuItem2);


            if (placedOrder.getMenuItems() != null && !placedOrder.getMenuItems().isEmpty()) {
                placedOrder.getMenuItems().forEach(menuItemId -> {
                    if (menuItemId != null) {
                        Boolean isAvailable = webClient
                                .get()
                                .uri(urlInventoryService + "/" + menuItemId.getOrderMenuItemIdKey().getMenuItemId() + "/availability")
                                .retrieve()
                                .onStatus(status -> status.is4xxClientError(), response -> {
                                    System.err.println("Client error: " + response.statusCode());
                                    return Mono.error(new RuntimeException("Client error occurred"));
                                })
                                .onStatus(status -> status.is5xxServerError(), response -> {
                                    System.err.println("Server error: " + response.statusCode());
                                    return Mono.error(new RuntimeException("Server error occurred"));
                                })
                                .bodyToMono(Boolean.class)
                                .block();
                        if (!isAvailable)
                            throw new ResourceNotFoundException("Menu item with ID " + menuItemId.getOrderMenuItemIdKey().getMenuItemId() + " is not available.");

                        // Step 3: Reduce the stock for the available menu item
                        webClient
                                .put()
                                .uri(urlInventoryService + "/" + menuItemId.getOrderMenuItemIdKey().getMenuItemId() + "/reduce")
                                .bodyValue(1)  // Assuming quantity is 1 for now
                                .retrieve()
                                .onStatus(status -> status.is4xxClientError(), response -> {
                                    System.err.println("Client error: " + response.statusCode());
                                    return Mono.error(new RuntimeException("Client error occurred"));
                                })
                                .onStatus(status -> status.is5xxServerError(), response -> {
                                    System.err.println("Server error: " + response.statusCode());
                                    return Mono.error(new RuntimeException("Server error occurred"));
                                })
                                .bodyToMono(Void.class)
                                .block();
                    } else
                        throw new ResourceNotFoundException("Menu item ID cannot be null.");
                });
            } else
                throw new ResourceNotFoundException("Order must contain at least one menu item.");


        double totalPrice = placedOrder.getMenuItems().stream()
                .mapToDouble(menuItemId -> {
                    Double price = webClient
                            .get()
                            .uri(urlMenuService + "/" + menuItemId.getOrderMenuItemIdKey().getMenuItemId() + "/price")
                            .retrieve()
                            .bodyToMono(Double.class)
                            .block();  // Blocking call
                    return price != null ? price : 0.0;
                })
                .sum();
        order.setTotal_price(totalPrice);
        order.setStatus("PENDING");

        return placedOrder;
    }
    @Override
    public PaginatedResponse<Order> getAllOrders(int page, int size, String[] sortBy, String[] direction) {
        List<Sort.Order> orders = IntStream.range(0, sortBy.length)
                .mapToObj(i -> new Sort.Order(Sort.Direction.fromString(direction[i]), sortBy[i]))
                .toList();
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<Order> ordersPage = this.orderDAOJPA.findAll(pageable);

        return new PaginatedResponse<>(
                ordersPage.getContent(),
                ordersPage.getNumber(),
                ordersPage.getTotalPages(),
                ordersPage.getTotalElements(),
                ordersPage.getSize());
    }

    @Override
    public Order getOrderById(Integer id) {
        return this.orderDAOJPA.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem with id: " + id + " not found."));
    }

    @Override
    @Transactional
    public Order createOrder(Order menuItem){
        try {
            return this.orderDAOJPA.save(menuItem);
        }catch(DataIntegrityViolationException e){
            throw new DatabaseUniqueValidationException(e.getRootCause().getMessage());
        }
    }
    @Override
    @Transactional
    public Order updateOrder(Integer id, Order order){
        Order foundOrder = getOrderById(id);

        foundOrder.setStatus(order.getStatus());
        foundOrder.setTotal_price(order.getTotal_price());
        foundOrder.setCustomerId(order.getCustomerId());

        try {
            return this.orderDAOJPA.save(foundOrder);
        }catch(DataIntegrityViolationException e){
            throw new DatabaseUniqueValidationException(e.getRootCause().getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteOrder(Integer id) {
        getOrderById(id);
        try {
            this.orderDAOJPA.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseUniqueValidationException(e.getRootCause().getMessage());
        }
    }
}
