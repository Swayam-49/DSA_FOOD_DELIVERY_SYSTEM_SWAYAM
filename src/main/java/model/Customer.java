package model;

import dsa.CustomStack;

/**
 * Represents a Customer in the system.
 * Demonstrates Inheritance (extends User) and Composition (has a Cart).
 */
public class Customer extends User {
    private String name;
    private String address;
    private String phone;
    private Cart cart;
    
    // Custom stack to store order history. Mark transient to prevent standard Gson serialization errors,
    // as it contains internal node links. We will manually serialize this into a flat list when requested.
    private final transient CustomStack<Order> orderHistory;

    public Customer(int id, String username, String password, String name, String address, String phone) {
        super(id, username, password, "CUSTOMER");
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.cart = new Cart();
        this.orderHistory = new CustomStack<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public CustomStack<Order> getOrderHistory() {
        return orderHistory;
    }
}
