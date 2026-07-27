package model;

/**
 * Represents a Delivery Partner in the system.
 * Inherits from User and manages ratings, availability status, and delivery tasks.
 */
public class DeliveryPartner extends User {
    private String name;
    private String phone;
    private double rating;
    private boolean isAvailable;
    private int currentOrderId; // -1 if no order is assigned

    public DeliveryPartner(int id, String username, String password, String name, String phone, double rating) {
        super(id, username, password, "DELIVERY");
        this.name = name;
        this.phone = phone;
        this.rating = rating;
        this.isAvailable = true;
        this.currentOrderId = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getCurrentOrderId() {
        return currentOrderId;
    }

    public void setCurrentOrderId(int currentOrderId) {
        this.currentOrderId = currentOrderId;
    }
}
