package app;

import dsa.CustomPriorityQueue;
import dsa.CustomQueue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import model.*;

/**
 * Centralized in-memory Data Store.
 * Pre-populates mock data for college viva demonstration purposes.
 * Keeps data alive during application runtime.
 */
public class DataStore {

    public static class Coupon {
        private final String code;
        private final double discountPercentage;
        private final double maxDiscount;

        public Coupon(String code, double discountPercentage, double maxDiscount) {
            this.code = code;
            this.discountPercentage = discountPercentage;
            this.maxDiscount = maxDiscount;
        }

        public String getCode() {
            return code;
        }

        public double getDiscountPercentage() {
            return discountPercentage;
        }

        public double getMaxDiscount() {
            return maxDiscount;
        }
    }

    // Main in-memory collections
    public static final ArrayList<Restaurant> restaurants = new ArrayList<>();
    public static final ArrayList<User> users = new ArrayList<>();
    public static final ArrayList<Order> orders = new ArrayList<>();

    // Custom Queue for pending orders waiting to be accepted by restaurants
    public static final CustomQueue<Order> pendingOrdersQueue = new CustomQueue<>();

    // Custom PriorityQueue (Max-Heap based on Rating) for available delivery partners
    public static final CustomPriorityQueue<DeliveryPartner> availableDeliverers = 
        new CustomPriorityQueue<>(new Comparator<DeliveryPartner>() {
            @Override
            public int compare(DeliveryPartner d1, DeliveryPartner d2) {
                return Double.compare(d2.getRating(), d1.getRating());
            }
        });

    // Lookup Maps
    public static final HashMap<Integer, User> userLookup = new HashMap<>();
    public static final HashMap<String, User> usernameLookup = new HashMap<>();
    public static final HashMap<String, Coupon> coupons = new HashMap<>();

    private static int orderIdCounter = 1;

    static {
        initializeData();
    }

    public static synchronized int getNextOrderId() {
        return orderIdCounter++;
    }

    private static void initializeData() {
        // 1. Populate Coupons
        coupons.put("QUICK10", new Coupon("QUICK10", 0.10, 5.0));
        coupons.put("EATS20", new Coupon("EATS20", 0.20, 10.0));
        coupons.put("BITE50", new Coupon("BITE50", 0.50, 15.0));

        // 2. Populate Admin
        Admin admin = new Admin(1001, "admin", "password");
        registerUser(admin);

        // 3. Populate 10 Customers
        Customer c1 = new Customer(1, "cust1", "password", "Alice Smith", "123 Main St, New York", "555-0101");
        Customer c2 = new Customer(2, "cust2", "password", "Bob Johnson", "456 Oak St, Brooklyn", "555-0102");
        Customer c3 = new Customer(3, "cust3", "password", "Charlie Brown", "789 Pine St, Queens", "555-0103");
        Customer c4 = new Customer(4, "cust4", "password", "Diana Prince", "101 Maple St, Manhattan", "555-0104");
        Customer c5 = new Customer(5, "cust5", "password", "Evan Wright", "202 Birch St, Bronx", "555-0105");
        Customer c6 = new Customer(6, "cust6", "password", "Fiona Gallagher", "303 Cedar St, Chicago", "555-0106");
        Customer c7 = new Customer(7, "cust7", "password", "George Costanza", "404 Elm St, Queens", "555-0107");
        Customer c8 = new Customer(8, "cust8", "password", "Hannah Abbott", "505 Spruce St, Boston", "555-0108");
        Customer c9 = new Customer(9, "cust9", "password", "Ian Malcolm", "606 Willow St, Austin", "555-0109");
        Customer c10 = new Customer(10, "cust10", "password", "Julia Roberts", "707 Walnut St, Los Angeles", "555-0110");

        registerUser(c1); registerUser(c2); registerUser(c3); registerUser(c4); registerUser(c5);
        registerUser(c6); registerUser(c7); registerUser(c8); registerUser(c9); registerUser(c10);

        // 4. Populate 5 Restaurants
        Restaurant r1 = new Restaurant(101, "rest1", "password", "Pizza Palazzo", "Italian", 4.7, 25);
        Restaurant r2 = new Restaurant(102, "rest2", "password", "Burger Bastion", "American", 4.4, 20);
        Restaurant r3 = new Restaurant(103, "rest3", "password", "Sushi Samurai", "Japanese", 4.8, 35);
        Restaurant r4 = new Restaurant(104, "rest4", "password", "Taco Temple", "Mexican", 4.2, 15);
        Restaurant r5 = new Restaurant(105, "rest5", "password", "Curry Castle", "Indian", 4.6, 30);

        // Add 5 menu items to each (25 total)
        r1.addFoodItem(new FoodItem(1011, "Margherita Pizza", 12.99, "Classic tomato, fresh mozzarella, basil", "Mains"));
        r1.addFoodItem(new FoodItem(1012, "Pepperoni Passion", 14.99, "Crispy pepperoni, cheese, tomato sauce", "Mains"));
        r1.addFoodItem(new FoodItem(1013, "Garlic Bread", 5.99, "Toasted baguette with garlic herb butter", "Starters"));
        r1.addFoodItem(new FoodItem(1014, "Fettuccine Alfredo", 16.99, "Creamy alfredo sauce over fettuccine", "Mains"));
        r1.addFoodItem(new FoodItem(1015, "Tiramisu", 6.99, "Classic coffee-flavoured Italian dessert", "Desserts"));

        r2.addFoodItem(new FoodItem(1021, "Classic Cheeseburger", 9.99, "Flame-grilled patty, cheddar, lettuce, tomato", "Mains"));
        r2.addFoodItem(new FoodItem(1022, "Bacon BBQ Burger", 11.99, "Smoked bacon, crispy onions, sweet BBQ sauce", "Mains"));
        r2.addFoodItem(new FoodItem(1023, "French Fries", 3.99, "Crisp, golden potato fries", "Starters"));
        r2.addFoodItem(new FoodItem(1024, "Onion Rings", 4.99, "Crunchy beer-battered onion rings", "Starters"));
        r2.addFoodItem(new FoodItem(1025, "Chocolate Milkshake", 5.49, "Thick and creamy chocolate milkshake", "Beverages"));

        r3.addFoodItem(new FoodItem(1031, "Salmon Roll", 7.99, "Fresh salmon, cucumber, avocado roll", "Mains"));
        r3.addFoodItem(new FoodItem(1032, "California Roll", 6.99, "Crab, cucumber, avocado topped with tobiko", "Mains"));
        r3.addFoodItem(new FoodItem(1033, "Spicy Tuna Roll", 8.99, "Minced spicy tuna and cucumber", "Mains"));
        r3.addFoodItem(new FoodItem(1034, "Tempura Udon", 13.99, "Hot udon noodles with crispy shrimp tempura", "Mains"));
        r3.addFoodItem(new FoodItem(1035, "Green Tea Mochi", 4.99, "Green tea ice cream wrapped in sweet rice dough", "Desserts"));

        r4.addFoodItem(new FoodItem(1041, "Beef Taco Combo", 8.99, "Three hard-shell beef tacos with lettuce and cheese", "Mains"));
        r4.addFoodItem(new FoodItem(1042, "Chicken Quesadilla", 9.99, "Grilled flour tortilla loaded with cheese and chicken", "Mains"));
        r4.addFoodItem(new FoodItem(1043, "Chips & Guacamole", 5.49, "Crispy tortilla chips with fresh house guacamole", "Starters"));
        r4.addFoodItem(new FoodItem(1044, "Burrito Bowl", 10.99, "Rice, black beans, pico de gallo, and choice of meat", "Mains"));
        r4.addFoodItem(new FoodItem(1045, "Churros", 4.49, "Golden fried churros dusted in cinnamon sugar", "Desserts"));

        r5.addFoodItem(new FoodItem(1051, "Butter Chicken", 15.99, "Tender chicken cooked in rich butter tomato gravy", "Mains"));
        r5.addFoodItem(new FoodItem(1052, "Garlic Naan", 2.99, "Soft tandoori flatbread topped with garlic and butter", "Starters"));
        r5.addFoodItem(new FoodItem(1053, "Vegetable Samosas", 4.99, "Spiced potato and pea fillings in a flaky pastry", "Starters"));
        r5.addFoodItem(new FoodItem(1054, "Chicken Biryani", 16.99, "Aromatic basmati rice cooked with chicken and spices", "Mains"));
        r5.addFoodItem(new FoodItem(1055, "Gulab Jamun", 4.49, "Deep-fried milk solids in rosewater cardamom syrup", "Desserts"));

        restaurants.add(r1); restaurants.add(r2); restaurants.add(r3); restaurants.add(r4); restaurants.add(r5);
        registerUser(r1); registerUser(r2); registerUser(r3); registerUser(r4); registerUser(r5);

        // 5. Populate 5 Delivery Partners
        DeliveryPartner d1 = new DeliveryPartner(201, "delivery1", "password", "Dave Miller", "555-0201", 4.8);
        DeliveryPartner d2 = new DeliveryPartner(202, "delivery2", "password", "Emma Watson", "555-0202", 4.6);
        DeliveryPartner d3 = new DeliveryPartner(203, "delivery3", "password", "Frank Castle", "555-0203", 4.9);
        DeliveryPartner d4 = new DeliveryPartner(204, "delivery4", "password", "Grace Hopper", "555-0204", 4.7);
        DeliveryPartner d5 = new DeliveryPartner(205, "delivery5", "password", "Harry Potter", "555-0205", 4.3);

        registerUser(d1); registerUser(d2); registerUser(d3); registerUser(d4); registerUser(d5);
        availableDeliverers.add(d1); availableDeliverers.add(d2); availableDeliverers.add(d3); 
        availableDeliverers.add(d4); availableDeliverers.add(d5);

        // 6. Pre-populate 10 Sample Orders to show metrics and history on startup
        createSampleOrder(c1, r1, List.of(new Cart.CartItem(r1.getMenu().get(0), 2)), "QUICK10", "DELIVERED", d1);
        createSampleOrder(c2, r2, List.of(new Cart.CartItem(r2.getMenu().get(0), 1), new Cart.CartItem(r2.getMenu().get(2), 2)), null, "DELIVERED", d2);
        createSampleOrder(c3, r3, List.of(new Cart.CartItem(r3.getMenu().get(1), 3)), "EATS20", "DELIVERED", d3);
        createSampleOrder(c4, r4, List.of(new Cart.CartItem(r4.getMenu().get(3), 2)), null, "DELIVERED", d4);
        createSampleOrder(c5, r5, List.of(new Cart.CartItem(r5.getMenu().get(0), 1), new Cart.CartItem(r5.getMenu().get(1), 2)), "QUICK10", "DELIVERED", d1);
        
        // Active orders in various stages
        createSampleOrder(c1, r2, List.of(new Cart.CartItem(r2.getMenu().get(1), 1)), null, "PENDING", null);
        createSampleOrder(c2, r3, List.of(new Cart.CartItem(r3.getMenu().get(0), 2)), null, "ACCEPTED", null);
        createSampleOrder(c3, r4, List.of(new Cart.CartItem(r4.getMenu().get(1), 2)), null, "PREPARING", null);
        createSampleOrder(c4, r5, List.of(new Cart.CartItem(r5.getMenu().get(3), 1)), "EATS20", "READY", d2);
        createSampleOrder(c5, r1, List.of(new Cart.CartItem(r1.getMenu().get(1), 1)), null, "PENDING", null);
    }

    private static void registerUser(User user) {
        users.add(user);
        userLookup.put(user.getId(), user);
        usernameLookup.put(user.getUsername().toLowerCase(), user);
    }

    private static void createSampleOrder(Customer customer, Restaurant restaurant, 
                                          List<Cart.CartItem> items, String couponCode, 
                                          String status, DeliveryPartner partner) {
        double subtotal = 0;
        for (Cart.CartItem ci : items) {
            subtotal += ci.getFoodItem().getPrice() * ci.getQuantity();
        }

        double discount = 0;
        if (couponCode != null && coupons.containsKey(couponCode)) {
            Coupon cp = coupons.get(couponCode);
            discount = subtotal * cp.getDiscountPercentage();
            if (discount > cp.getMaxDiscount()) {
                discount = cp.getMaxDiscount();
            }
        }
        double finalPrice = subtotal - discount;

        Order order = new Order(getNextOrderId(), customer.getId(), restaurant.getId(), 
                                items, subtotal, couponCode, discount, finalPrice);
        order.setStatus(status);

        if (partner != null) {
            order.setDeliveryPartnerId(partner.getId());
            // If it's not delivered yet, mark partner as busy
            if (!"DELIVERED".equals(status)) {
                partner.setAvailable(false);
                partner.setCurrentOrderId(order.getOrderId());
            }
        }

        orders.add(order);

        // Put in customer history (Stack)
        customer.getOrderHistory().push(order);

        // If PENDING, put in the pending queue
        if ("PENDING".equals(status)) {
            pendingOrdersQueue.enqueue(order);
        }
    }

    /**
     * Helper to retrieve all delivery partners.
     */
    public static List<DeliveryPartner> getAllDeliveryPartners() {
        List<DeliveryPartner> dpList = new ArrayList<>();
        for (User u : users) {
            if (u instanceof DeliveryPartner) {
                dpList.add((DeliveryPartner) u);
            }
        }
        return dpList;
    }

    /**
     * Helper to retrieve all customers.
     */
    public static List<Customer> getAllCustomers() {
        List<Customer> custList = new ArrayList<>();
        for (User u : users) {
            if (u instanceof Customer) {
                custList.add((Customer) u);
            }
        }
        return custList;
    }
}
