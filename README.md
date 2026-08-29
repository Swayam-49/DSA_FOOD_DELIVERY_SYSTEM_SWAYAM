# QuickBites – Local Food Delivery System

QuickBites is a complete, browser-based, light-weight local food delivery web application built from scratch in Java. This project was developed as a final academic project to demonstrate Object-Oriented Programming (OOP), Data Structures and Algorithms (DSA), and Clean Architecture principles.

## 🚀 Key Features

*   **Multi-Role Logins:** Dedicated portals and actions for:
    *   **Customer:** Browse, search (Binary Search), sort restaurants (Merge Sort), manage cart, checkout with coupons, and track active delivery status.
    *   **Restaurant:** Accept/reject incoming orders, manage the kitchen queue, and trigger dispatch assignments.
    *   **Delivery Partner:** View assigned jobs, accept orders, complete pick-ups, and mark deliveries.
    *   **Admin:** Monitor statistics (total customers, active orders, rider metrics) and review live order logs.
*   **Built-in Web Server:** Runs locally using Java's built-in `HttpServer` and serves static files and custom JSON REST APIs.
*   **In-Memory Storage:** Pure Java collections representing database entries, pre-populated with ample demo data.
*   **Coupon Discounts:** Apply codes like `QUICK10`, `EATS20`, or `BITE50` for real-time calculations.

---

## 🛠️ Tech Stack

*   **Backend:** Java 17+, Maven, Java built-in `HttpServer`, Google Gson (JSON handler).
*   **Frontend:** HTML5, CSS3 (Vanilla System), Bootstrap 5, Javascript (ES6 AJAX).

---

## 📁 Folder Structure

```
QuickBites/
├── pom.xml
├── src/main/java/
│   ├── app/
│   │   ├── Main.java         # Server launcher & browser automation
│   │   ├── Router.java       # HTTP router (static files & JSON API)
│   │   └── DataStore.java    # Mock Database pre-populated in memory
│   ├── model/
│   │   ├── User.java         # OOP Hierarchy (Customer, Restaurant, DeliveryPartner, Admin)
│   │   ├── FoodItem.java     # Menu items schema
│   │   ├── Cart.java         # Customer cart (Composition of CartItems)
│   │   └── Order.java        # Transaction details & status tracker
│   └── dsa/
│       ├── Sort.java         # Custom Merge Sort implementation
│       ├── Search.java       # Custom Binary Search implementation
│       ├── CustomQueue.java  # Custom LinkedList FIFO Queue (Pending Orders)
│       ├── CustomStack.java  # Custom LIFO Stack (Order History)
│       └── CustomPriorityQueue.java # Custom Heap Priority Queue (Rider selection)
└── frontend/                 # Web assets (HTML, CSS, JS)
```

---

## 📦 Data Structures & Algorithms Used

1.  **Custom LinkedList Queue (`CustomQueue`):** Holds pending orders. Offers $O(1)$ enqueuing and dequeuing.
2.  **Custom Resizing Stack (`CustomStack`):** Tracks customer order history. Implements LIFO so that the newest order appears first.
3.  **Custom Binary Heap (`CustomPriorityQueue`):** Manages available delivery riders, assigning the closest/highest-rated available partner first.
4.  **Custom Merge Sort (`Sort`):** Sorts restaurants dynamically by Name, Rating (descending), or Delivery Speed (ascending) with $O(N \log N)$ worst-case time complexity.
5.  **Custom Binary Search (`Search`):** Searches restaurants by name in $O(\log N)$ after matching sorted criteria.

---

## 💻 Run Instructions

### Prerequisites
*   Java Development Kit (JDK) 17 or higher.
*   Apache Maven installed (optional, or run direct in IDE).

### Steps
1.  **Open in IntelliJ IDEA** (or Eclipse / VS Code).
2.  Navigate to `src/main/java/app/Main.java`.
3.  Right-click `Main.java` and select **Run**.
4.  The program starts a local server on **`http://localhost:8080`** and opens the web browser automatically.
5.  *Alternatively, via Terminal:*
    ```bash
    mvn clean compile exec:java
    ```

---

## 🛡️ Default Test Accounts

| Role | Username | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Customer** | `cust1` to `cust10` | `password` | Search, buy food, apply coupons, and track orders. |
| **Restaurant** | `rest1` to `rest5` | `password` | Accept/reject orders, process kitchen states. |
| **Rider** | `delivery1` to `delivery5` | `password` | Accept deliveries, pick up and drop food. |
| **Admin** | `admin` | `password` | Review system telemetry. |
