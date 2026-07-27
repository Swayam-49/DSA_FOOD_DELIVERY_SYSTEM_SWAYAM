package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the customer's shopping cart.
 * Contains items from a single restaurant at a time.
 */
public class Cart {

    /**
     * Inner class representing an item and its selected quantity in the cart.
     */
    public static class CartItem {
        private final FoodItem foodItem;
        private int quantity;

        public CartItem(FoodItem foodItem, int quantity) {
            this.foodItem = foodItem;
            this.quantity = quantity;
        }

        public FoodItem getFoodItem() {
            return foodItem;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    private final List<CartItem> items;
    private int restaurantId; // Cart can only hold items from one restaurant at a time

    public Cart() {
        this.items = new ArrayList<>();
        this.restaurantId = -1;
    }

    /**
     * Adds an item to the cart. If the item is from a different restaurant,
     * the cart is cleared first (standard food delivery behavior).
     */
    public synchronized void addItem(FoodItem item, int quantity, int targetRestaurantId) {
        if (this.restaurantId != targetRestaurantId) {
            clear();
            this.restaurantId = targetRestaurantId;
        }

        for (CartItem ci : items) {
            if (ci.getFoodItem().getId() == item.getId()) {
                ci.setQuantity(ci.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(item, quantity));
    }

    /**
     * Updates the quantity of a specific item. If quantity <= 0, removes the item.
     */
    public synchronized void updateQuantity(int itemId, int quantity) {
        CartItem itemToRemove = null;
        for (CartItem ci : items) {
            if (ci.getFoodItem().getId() == itemId) {
                if (quantity <= 0) {
                    itemToRemove = ci;
                } else {
                    ci.setQuantity(quantity);
                }
                break;
            }
        }

        if (itemToRemove != null) {
            items.remove(itemToRemove);
        }

        if (items.isEmpty()) {
            this.restaurantId = -1;
        }
    }

    /**
     * Removes an item from the cart.
     */
    public synchronized void removeItem(int itemId) {
        items.removeIf(ci -> ci.getFoodItem().getId() == itemId);
        if (items.isEmpty()) {
            this.restaurantId = -1;
        }
    }

    /**
     * Clears all items in the cart.
     */
    public synchronized void clear() {
        items.clear();
        this.restaurantId = -1;
    }

    // Getters
    public synchronized List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public synchronized int getRestaurantId() {
        return restaurantId;
    }

    /**
     * Computes the total price of all items in the cart.
     */
    public synchronized double getTotalPrice() {
        double total = 0;
        for (CartItem ci : items) {
            total += ci.getFoodItem().getPrice() * ci.getQuantity();
        }
        return total;
    }
}
