package model;

import java.util.ArrayList;

/**
 * Represents a Restaurant in the QuickBites system.
 * Inherits from User and contains cuisine information, rating, delivery time, and menu.
 */
public class Restaurant extends User {
    private String name;
    private String cuisineType;
    private double rating;
    private int deliveryTime; // in minutes
    private ArrayList<FoodItem> menu;

    public Restaurant(int id, String username, String password, String name, String cuisineType, double rating, int deliveryTime) {
        super(id, username, password, "RESTAURANT");
        this.name = name;
        this.cuisineType = cuisineType;
        this.rating = rating;
        this.deliveryTime = deliveryTime;
        this.menu = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(int deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public ArrayList<FoodItem> getMenu() {
        return menu;
    }

    public void setMenu(ArrayList<FoodItem> menu) {
        this.menu = menu;
    }

    public void addFoodItem(FoodItem item) {
        this.menu.add(item);
    }
}
