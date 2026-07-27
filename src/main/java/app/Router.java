package app;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dsa.DeliveryManager;
import dsa.Search;
import dsa.Sort;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import model.*;

/**
 * Handles HTTP request routing for serving static files and processing JSON API requests.
 */
public class Router implements HttpHandler {

    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Log incoming request details (useful for local development)
        System.out.println("[Request] " + method + " " + path);

        try {
            // API endpoints are prefixed with /api/
            if (path.startsWith("/api/")) {
                handleApiRequest(exchange, path, method);
            } else {
                serveStaticFile(exchange, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void serveStaticFile(HttpExchange exchange, String path) throws IOException {
        // Default to index.html for root path
        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        // Clean double slashes
        path = path.replace("//", "/");

        // Prevent directory traversal attacks
        if (path.contains("..")) {
            sendError(exchange, 403, "Forbidden");
            return;
        }

        File file = new File("frontend" + path);
        if (!file.exists() || file.isDirectory()) {
            // Fallback: if user refreshes on an HTML path without extension, serve index.html or redirect
            sendError(exchange, 404, "File Not Found: " + path);
            return;
        }

        // Guess MIME content type
        String contentType = "text/plain";
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".html")) {
            contentType = "text/html; charset=utf-8";
        } else if (lowerPath.endsWith(".css")) {
            contentType = "text/css; charset=utf-8";
        } else if (lowerPath.endsWith(".js")) {
            contentType = "application/javascript; charset=utf-8";
        } else if (lowerPath.endsWith(".png")) {
            contentType = "image/png";
        } else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (lowerPath.endsWith(".svg")) {
            contentType = "image/svg+xml";
        } else if (lowerPath.endsWith(".json")) {
            contentType = "application/json; charset=utf-8";
        }

        byte[] bytes = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void handleApiRequest(HttpExchange exchange, String path, String method) throws IOException {
        // CORS headers for local testing
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

        switch (path) {
            case "/api/login":
                if ("POST".equalsIgnoreCase(method)) {
                    processLogin(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            case "/api/restaurants":
                if ("GET".equalsIgnoreCase(method)) {
                    listRestaurants(exchange, queryParams);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            case "/api/restaurant":
                if ("GET".equalsIgnoreCase(method)) {
                    getRestaurant(exchange, queryParams);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            case "/api/cart":
                if ("GET".equalsIgnoreCase(method)) {
                    getCart(exchange, queryParams);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            case "/api/cart/add":
                if ("POST".equalsIgnoreCase(method)) {
                    addToCart(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            case "/api/cart/update":
                if ("POST".equalsIgnoreCase(method)) {
                    updateCartQuantity(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            case "/api/cart/remove":
                if ("POST".equalsIgnoreCase(method)) {
                    removeFromCart(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            case "/api/order/place":
                if ("POST".equalsIgnoreCase(method)) {
                    placeOrder(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            case "/api/orders":
                if ("GET".equalsIgnoreCase(method)) {
                    getOrders(exchange, queryParams);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            case "/api/order/status":
                if ("POST".equalsIgnoreCase(method)) {
                    updateOrderStatus(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            case "/api/admin/stats":
                if ("GET".equalsIgnoreCase(method)) {
                    getAdminStats(exchange);
                } else {
                    sendError(exchange, 405, "Method Not Allowed");
                }
                break;

            default:
                sendError(exchange, 404, "API Route Not Found");
                break;
        }
    }

    // ==========================================
    // API Route Handlers
    // ==========================================

    private void processLogin(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
        
        String username = json.get("username").getAsString().trim().toLowerCase();
        String password = json.get("password").getAsString();
        String role = json.get("role").getAsString();

        User user = DataStore.usernameLookup.get(username);
        Map<String, Object> response = new HashMap<>();

        if (user != null && user.getPassword().equals(password) && user.getRole().equalsIgnoreCase(role)) {
            response.put("success", true);
            response.put("user", user);
            sendJsonResponse(exchange, 200, response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid username, password, or role choice.");
            sendJsonResponse(exchange, 401, response);
        }
    }

    private void listRestaurants(HttpExchange exchange, Map<String, String> params) throws IOException {
        String search = params.get("search");
        String sortBy = params.get("sortBy");

        List<Restaurant> resultList;

        if (search != null && !search.trim().isEmpty()) {
            // Binary Search expects exact target match, but we can make it case-insensitive
            Restaurant match = Search.binarySearchByName(DataStore.restaurants, search.trim());
            resultList = new ArrayList<>();
            if (match != null) {
                resultList.add(match);
            } else {
                // Fallback prefix search: if binary search fails to find exact match, 
                // we can do a quick linear search for matching substring to make search user-friendly!
                for (Restaurant r : DataStore.restaurants) {
                    if (r.getName().toLowerCase().contains(search.toLowerCase())) {
                        resultList.add(r);
                    }
                }
            }
        } else {
            resultList = new ArrayList<>(DataStore.restaurants);
        }

        // Apply merge sort if sorting criteria specified
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            Sort.mergeSort(resultList, sortBy.trim());
        }

        sendJsonResponse(exchange, 200, resultList);
    }

    private void getRestaurant(HttpExchange exchange, Map<String, String> params) throws IOException {
        String idStr = params.get("id");
        if (idStr == null) {
            sendError(exchange, 400, "Missing restaurant ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            User user = DataStore.userLookup.get(id);
            if (user instanceof Restaurant) {
                sendJsonResponse(exchange, 200, user);
            } else {
                sendError(exchange, 404, "Restaurant not found");
            }
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Invalid restaurant ID format");
        }
    }

    private void getCart(HttpExchange exchange, Map<String, String> params) throws IOException {
        String userIdStr = params.get("userId");
        if (userIdStr == null) {
            sendError(exchange, 400, "Missing user ID");
            return;
        }

        Customer customer = getCustomer(userIdStr);
        if (customer == null) {
            sendError(exchange, 404, "Customer not found");
            return;
        }

        Map<String, Object> cartDetails = new HashMap<>();
        cartDetails.put("items", customer.getCart().getItems());
        cartDetails.put("totalPrice", customer.getCart().getTotalPrice());
        cartDetails.put("restaurantId", customer.getCart().getRestaurantId());

        sendJsonResponse(exchange, 200, cartDetails);
    }

    private void addToCart(HttpExchange exchange) throws IOException {
        JsonObject json = JsonParser.parseString(readRequestBody(exchange)).getAsJsonObject();
        String userIdStr = json.get("userId").getAsString();
        int itemId = json.get("itemId").getAsInt();
        int quantity = json.get("quantity").getAsInt();
        int restaurantId = json.get("restaurantId").getAsInt();

        Customer customer = getCustomer(userIdStr);
        if (customer == null) {
            sendError(exchange, 404, "Customer not found");
            return;
        }

        // Find the food item from data store restaurants
        FoodItem item = null;
        for (Restaurant r : DataStore.restaurants) {
            if (r.getId() == restaurantId) {
                for (FoodItem fi : r.getMenu()) {
                    if (fi.getId() == itemId) {
                        item = fi;
                        break;
                    }
                }
            }
        }

        if (item == null) {
            sendError(exchange, 404, "Food item not found");
            return;
        }

        customer.getCart().addItem(item, quantity, restaurantId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cart", customer.getCart());
        sendJsonResponse(exchange, 200, response);
    }

    private void updateCartQuantity(HttpExchange exchange) throws IOException {
        JsonObject json = JsonParser.parseString(readRequestBody(exchange)).getAsJsonObject();
        String userIdStr = json.get("userId").getAsString();
        int itemId = json.get("itemId").getAsInt();
        int quantity = json.get("quantity").getAsInt();

        Customer customer = getCustomer(userIdStr);
        if (customer == null) {
            sendError(exchange, 404, "Customer not found");
            return;
        }

        customer.getCart().updateQuantity(itemId, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cart", customer.getCart());
        sendJsonResponse(exchange, 200, response);
    }

    private void removeFromCart(HttpExchange exchange) throws IOException {
        JsonObject json = JsonParser.parseString(readRequestBody(exchange)).getAsJsonObject();
        String userIdStr = json.get("userId").getAsString();
        int itemId = json.get("itemId").getAsInt();

        Customer customer = getCustomer(userIdStr);
        if (customer == null) {
            sendError(exchange, 404, "Customer not found");
            return;
        }

        customer.getCart().removeItem(itemId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cart", customer.getCart());
        sendJsonResponse(exchange, 200, response);
    }

    private void placeOrder(HttpExchange exchange) throws IOException {
        JsonObject json = JsonParser.parseString(readRequestBody(exchange)).getAsJsonObject();
        String userIdStr = json.get("userId").getAsString();
        String couponCode = json.has("couponCode") ? json.get("couponCode").getAsString().trim().toUpperCase() : "";

        Customer customer = getCustomer(userIdStr);
        if (customer == null) {
            sendError(exchange, 404, "Customer not found");
            return;
        }

        Cart cart = customer.getCart();
        if (cart.getItems().isEmpty()) {
            sendError(exchange, 400, "Cart is empty");
            return;
        }

        double subtotal = cart.getTotalPrice();
        double discount = 0;
        
        if (!couponCode.isEmpty() && DataStore.coupons.containsKey(couponCode)) {
            DataStore.Coupon cp = DataStore.coupons.get(couponCode);
            discount = subtotal * cp.getDiscountPercentage();
            if (discount > cp.getMaxDiscount()) {
                discount = cp.getMaxDiscount();
            }
        }
        
        double finalPrice = subtotal - discount;
        int orderId = DataStore.getNextOrderId();

        Order order = new Order(orderId, customer.getId(), cart.getRestaurantId(), 
                                cart.getItems(), subtotal, couponCode, discount, finalPrice);
        
        // Add to Datastore
        synchronized (DataStore.orders) {
            DataStore.orders.add(order);
        }
        
        // Put in customer's order history stack (LIFO)
        customer.getOrderHistory().push(order);

        // Put in pending queue
        DataStore.pendingOrdersQueue.enqueue(order);

        // Clear cart
        cart.clear();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("order", order);
        sendJsonResponse(exchange, 200, response);
    }

    private void getOrders(HttpExchange exchange, Map<String, String> params) throws IOException {
        String userIdStr = params.get("userId");
        String role = params.get("role");

        if (userIdStr == null || role == null) {
            sendError(exchange, 400, "Missing query parameters");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Invalid user ID format");
            return;
        }

        List<Order> result = new ArrayList<>();

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            Customer customer = getCustomer(userIdStr);
            if (customer != null) {
                // Return LIFO order history from Stack
                result = customer.getOrderHistory().toList();
            }
        } else if ("RESTAURANT".equalsIgnoreCase(role)) {
            synchronized (DataStore.orders) {
                for (Order o : DataStore.orders) {
                    if (o.getRestaurantId() == userId) {
                        result.add(o);
                    }
                }
            }
            // Reverse to show newest at the top
            Collections.reverse(result);
        } else if ("DELIVERY".equalsIgnoreCase(role)) {
            synchronized (DataStore.orders) {
                for (Order o : DataStore.orders) {
                    if (o.getDeliveryPartnerId() == userId) {
                        result.add(o);
                    }
                }
            }
            Collections.reverse(result);
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            synchronized (DataStore.orders) {
                result = new ArrayList<>(DataStore.orders);
            }
            Collections.reverse(result);
        }

        sendJsonResponse(exchange, 200, result);
    }

    private void updateOrderStatus(HttpExchange exchange) throws IOException {
        JsonObject json = JsonParser.parseString(readRequestBody(exchange)).getAsJsonObject();
        int orderId = json.get("orderId").getAsInt();
        String status = json.get("status").getAsString().toUpperCase();
        int userId = json.get("userId").getAsInt();

        Order order = null;
        synchronized (DataStore.orders) {
            for (Order o : DataStore.orders) {
                if (o.getOrderId() == orderId) {
                    order = o;
                    break;
                }
            }
        }

        if (order == null) {
            sendError(exchange, 404, "Order not found");
            return;
        }

        Map<String, Object> response = new HashMap<>();

        // Status transitions
        synchronized (order) {
            if ("ACCEPTED".equals(status)) {
                // Restaurant accepts order
                order.setStatus("ACCEPTED");
            } else if ("REJECTED".equals(status)) {
                // Restaurant rejects order
                order.setStatus("REJECTED");
            } else if ("PREPARING".equals(status)) {
                // Restaurant marks preparing
                order.setStatus("PREPARING");
            } else if ("READY".equals(status)) {
                // Restaurant marks ready, triggers priority delivery assignment
                List<DeliveryPartner> allPartners = DataStore.getAllDeliveryPartners();
                DeliveryPartner assigned = DeliveryManager.assignPartner(order, allPartners);
                if (assigned != null) {
                    response.put("assignedPartner", assigned);
                } else {
                    order.setStatus("READY"); // Stays ready, wait for partner
                }
            } else if ("ACCEPTED_DELIVERY".equals(status)) {
                // Delivery partner accepts assigned delivery
                order.setStatus("ACCEPTED_DELIVERY");
            } else if ("PICKED_UP".equals(status)) {
                // Delivery partner picks up food
                order.setStatus("PICKED_UP");
            } else if ("DELIVERED".equals(status)) {
                // Delivery partner completes delivery
                order.setStatus("DELIVERED");

                // Free up the delivery partner
                DeliveryPartner dp = (DeliveryPartner) DataStore.userLookup.get(order.getDeliveryPartnerId());
                if (dp != null) {
                    dp.setAvailable(true);
                    dp.setCurrentOrderId(-1);

                    // Re-enqueue the partner back into available heap
                    DataStore.availableDeliverers.add(dp);

                    // Check if there are other READY orders waiting for assignment
                    assignDeliveryToWaitingOrders();
                }
            }
        }

        response.put("success", true);
        response.put("order", order);
        sendJsonResponse(exchange, 200, response);
    }

    /**
     * Scans for orders with "READY" status and no delivery partner,
     * assigning them to any now-available delivery partners.
     */
    private void assignDeliveryToWaitingOrders() {
        synchronized (DataStore.orders) {
            for (Order o : DataStore.orders) {
                if ("READY".equals(o.getStatus()) && o.getDeliveryPartnerId() == -1) {
                    List<DeliveryPartner> partners = DataStore.getAllDeliveryPartners();
                    DeliveryPartner dp = DeliveryManager.assignPartner(o, partners);
                    if (dp != null) {
                        System.out.println("[Delivery Assignment] Auto-assigned Order #" + o.getOrderId() + " to " + dp.getName());
                        break; // assigned one, break to let PQ handle order-by-order
                    }
                }
            }
        }
    }

    private void getAdminStats(HttpExchange exchange) throws IOException {
        int totalRestaurants = DataStore.restaurants.size();
        int totalCustomers = DataStore.getAllCustomers().size();
        int totalOrders;
        int activeOrders = 0;
        int deliveredOrders = 0;

        synchronized (DataStore.orders) {
            totalOrders = DataStore.orders.size();
            for (Order o : DataStore.orders) {
                if ("DELIVERED".equals(o.getStatus())) {
                    deliveredOrders++;
                } else if (!"REJECTED".equals(o.getStatus())) {
                    activeOrders++;
                }
            }
        }

        int availableDeliverersCount = 0;
        for (DeliveryPartner dp : DataStore.getAllDeliveryPartners()) {
            if (dp.isAvailable()) {
                availableDeliverersCount++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRestaurants", totalRestaurants);
        stats.put("totalCustomers", totalCustomers);
        stats.put("totalOrders", totalOrders);
        stats.put("activeOrders", activeOrders);
        stats.put("deliveredOrders", deliveredOrders);
        stats.put("availableDeliverersCount", availableDeliverersCount);

        sendJsonResponse(exchange, 200, stats);
    }

    // ==========================================
    // Utility Helpers
    // ==========================================

    private Customer getCustomer(String userIdStr) {
        try {
            int id = Integer.parseInt(userIdStr);
            User u = DataStore.userLookup.get(id);
            if (u instanceof Customer) {
                return (Customer) u;
            }
        } catch (NumberFormatException e) {
            // Ignored
        }
        return null;
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        try {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                String key = URLDecoder.decode(entry[0], StandardCharsets.UTF_8.name());
                String val = entry.length > 1 ? URLDecoder.decode(entry[1], StandardCharsets.UTF_8.name()) : "";
                result.put(key, val);
            }
        } catch (UnsupportedEncodingException e) {
            // Should not happen as UTF-8 is supported
        }
        return result;
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object responseObj) throws IOException {
        String jsonResponse = gson.toJson(responseObj);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("success", false);
        errorMap.put("message", message);
        sendJsonResponse(exchange, statusCode, errorMap);
    }
}
