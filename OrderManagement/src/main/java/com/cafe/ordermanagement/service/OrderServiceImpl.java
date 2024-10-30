package com.cafe.ordermanagement.service;

import com.cafe.ordermanagement.dto.MenuItem;
import com.cafe.ordermanagement.entity.Category;
import com.cafe.ordermanagement.entity.OrderMenuItemId;
import com.cafe.ordermanagement.entity.OrderMenuItemIdKey;
import com.cafe.ordermanagement.exception.*;
import com.cafe.ordermanagement.dao.OrderDAOJPA;
import com.cafe.ordermanagement.entity.Order;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    private final OrderDAOJPA orderDAOJPA;
    @Autowired
    private WebClient webClient;
    @Value("${menu.service.url}")
    private String menuServiceUrl;
    @Value("${menu.service.url}")
    private String menuServiceCategoryUrl;
    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public OrderServiceImpl(OrderDAOJPA orderDAOJPA, WebClient webClient){
        this.orderDAOJPA = orderDAOJPA;
        this.webClient = webClient;
    }
    @PostConstruct
    private void init() {
        menuServiceUrl += "/api/menuitems";
        menuServiceCategoryUrl += "/api/categories";
        inventoryServiceUrl += "/api/inventory";
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

    public PaginatedResponse<MenuItem> getAllMenuItems(int page, int size, String[] sortBy, String[] direction) {
        String uri = UriComponentsBuilder.fromHttpUrl(menuServiceUrl)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", (Object[]) sortBy)
                .queryParam("direction", (Object[]) direction)
                .toUriString();

        return webClient.get()
            .uri(uri)
            .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new ClientErrorException("Client error: " + errorBody);}))
                .onStatus(
                        status -> status.is5xxServerError(), response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new ServerErrorException("Server error: " + errorBody);}))
            .bodyToMono(new ParameterizedTypeReference<PaginatedResponse<MenuItem>>() {})
            .block();
    }

    @Override
    public PaginatedResponse<Category> getAllMenuItemCategories(
            int page, int size, String[] sortBy, String[] direction) {
        String categoryUri = UriComponentsBuilder.fromHttpUrl(menuServiceCategoryUrl)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", (Object[]) sortBy)
                .queryParam("direction", (Object[]) direction)
                .toUriString();

        return webClient.get()
                .uri(categoryUri)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new ClientErrorException("Client error: " + errorBody);
                                }))
                .onStatus(
                        status -> status.is5xxServerError(), response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new ServerErrorException("Server error: " + errorBody);
                                }))
                .bodyToMono(new ParameterizedTypeReference<PaginatedResponse<Category>>() {
                })
                .block();
    }

    @Override
    public PaginatedResponse<MenuItem> getAllMenuItemsByCategory(
            int page, int size, String[] sortBy, String[] direction, String categoryName){
        String uri = UriComponentsBuilder.fromHttpUrl(menuServiceUrl + "/filter/category-name")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", (Object[]) sortBy)
                .queryParam("direction", (Object[]) direction)
                .queryParam("categoryName", categoryName)
                .toUriString();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new ClientErrorException("Client error: " + errorBody);}))
                .onStatus(
                        status -> status.is5xxServerError(), response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new ServerErrorException("Server error: " + errorBody);}))
                .bodyToMono(new ParameterizedTypeReference<PaginatedResponse<MenuItem>>() {})
                .block();
    }

    @Override
    public Map<String, List<MenuItem>> getMenuItemsGroupedByCategory(
            int page, int size, String[] sortBy, String[] direction){
        Map<String, List<MenuItem>> categorizedMenuItems = new LinkedHashMap<>();
                List<Category> categories =
                this.getAllMenuItemCategories(page, size, sortBy, direction).getData();

        categories.stream().forEach(
                category -> {categorizedMenuItems.put(
                        category.getName(), this.getAllMenuItemsByCategory(page, size, sortBy, direction, category.getName()).getData());});

        return categorizedMenuItems;
    }

    //getAllAvailableItems
    //wyswietla liste tylko dostepnych menuitems i pozniej nie musimy juz sprawdzac dostepnosci
    //nadal trzeba sprawdzic ilosc

    //wyswietla liste wszystkich menuitems
    //uzytkownik wybiera co chce i klika placeOrder
    //sprawdzmy czy wszytskie sa dostepne
    // TODO: update readme then we can remove this comment
    @Override
    @Transactional
    public Order placeOrder(Order order,
                            List<Integer> menuItemIds,
                            List<Integer> quantitiesOfMenuItems) {

        List<Integer> filteredQuantities = quantitiesOfMenuItems.stream()
                .filter(quantity -> !quantity.equals(0))
                .collect(Collectors.toList());

        Order placedOrder = createOrder(order);
        menuItemIds.stream().forEach(menuItemId -> {
            OrderMenuItemId orderMenuItemId = new OrderMenuItemId(
                    new OrderMenuItemIdKey(placedOrder.getId(), menuItemId, filteredQuantities.get(menuItemIds.indexOf(menuItemId))));
            placedOrder.addMenuItem(orderMenuItemId);
        });

        if (placedOrder.getMenuItems() == null || placedOrder.getMenuItems().isEmpty())
            throw new ResourceNotFoundException("Choose at least one Menu Item.");

        // Check if each selected MenuItem with choosen quantity is available
        List<Boolean> areMenuItemsAvailable = webClient.get()
                .uri(UriComponentsBuilder
                        .fromHttpUrl(inventoryServiceUrl + "/availability")
                        .queryParam("menuItemIds", menuItemIds.toArray())
                        .queryParam("quantitiesOfMenuItems", filteredQuantities.toArray())
                        .toUriString())
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new ClientErrorException("Client error: " + errorBody);}))
                .onStatus(
                        status -> status.is5xxServerError(), response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new ServerErrorException("Server error: " + errorBody);}))
                .bodyToMono(new ParameterizedTypeReference<List<Boolean>>() {})
                .block();

        IntStream.range(0, areMenuItemsAvailable.size())
                .filter(i -> !areMenuItemsAvailable.get(i))
                .findFirst()
                .ifPresent(i -> {
                    throw new ResourceNotFoundException("Menu item with ID: " + menuItemIds.get(i) + " is not available.");
                });

        // Reduce the stock for each selected MenuItem with choosen quantity
        webClient.put()
                .uri(UriComponentsBuilder
                        .fromHttpUrl(inventoryServiceUrl + "/reduce")
                        .queryParam("menuItemIds", menuItemIds.toArray())
                        .queryParam("quantitiesOfMenuItems", filteredQuantities.toArray())
                        .toUriString())
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new ClientErrorException("Client error: " + errorBody);}))
                .onStatus(
                        status -> status.is5xxServerError(), response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    throw new ServerErrorException("Server error: " + errorBody);}))
                .bodyToMono(Void.class)
                .block();

        // Calculate total price and update status
        double totalPrice = placedOrder.getMenuItems().stream()
                .mapToDouble(menuItemId -> {
                    Double price = webClient
                            .get()
                            .uri(menuServiceUrl + "/" + menuItemId.getOrderMenuItemIdKey().getMenuItemId() + "/price")
                            .retrieve()
                            .bodyToMono(Double.class)
                            .block();
                    return price != null ? price : 0.0;
                })
                .sum();
        order.setTotal_price(totalPrice);
        order.setStatus("PENDING");

        return placedOrder;
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
