package model;

import java.util.List;

/**
 * Represents an Order placed by a customer.
 */
public class Order {
    private int orderId;
    private int customerId;
    private int restaurantId;
    private List<Cart.CartItem> items;
    private double totalPrice;
    private String couponCode;
    private double discount;
    private double finalPrice;
    private String status; // "PENDING", "ACCEPTED", "PREPARING", "READY", "DELIVERED", "REJECTED"
    private int deliveryPartnerId; // -1 if not assigned
    private long timestamp;

    public Order(int orderId, int customerId, int restaurantId, List<Cart.CartItem> items, 
                 double totalPrice, String couponCode, double discount, double finalPrice) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.couponCode = couponCode;
        this.discount = discount;
        this.finalPrice = finalPrice;
        this.status = "PENDING"; // initial status
        this.deliveryPartnerId = -1; // unassigned
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<Cart.CartItem> getItems() {
        return items;
    }

    public void setItems(List<Cart.CartItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDeliveryPartnerId() {
        return deliveryPartnerId;
    }

    public void setDeliveryPartnerId(int deliveryPartnerId) {
        this.deliveryPartnerId = deliveryPartnerId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
