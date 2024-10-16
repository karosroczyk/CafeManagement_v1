package com.cafe.ordermanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class OrderMenuItemIdKey implements Serializable {
    @Column(name = "order_id")
    private Integer orderId;
    @Column(name = "menuItem_id")
    private Integer menuItemId;
    public OrderMenuItemIdKey() {}

    public OrderMenuItemIdKey(Integer orderId, Integer menuItemId) {
        this.orderId = orderId;
        this.menuItemId = menuItemId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Integer menuItemId) {
        this.menuItemId = menuItemId;
    }

    @Override
    public String toString() {
        return "OrderMenuItemIdKey{" +
                "orderId=" + orderId +
                ", menuItemId=" + menuItemId +
                '}';
    }
}
