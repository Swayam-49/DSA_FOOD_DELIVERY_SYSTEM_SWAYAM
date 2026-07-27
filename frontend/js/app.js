// QuickBites Central JavaScript File
// Manages authentication state, AJAX requests, Cart operations, and DOM rendering.

document.addEventListener("DOMContentLoaded", () => {
    renderNavbar();
    
    // Auto-run page-specific logic based on current page
    const pathname = window.location.pathname;
    if (pathname.endsWith("index.html") || pathname === "/") {
        initBrowsePage();
    } else if (pathname.endsWith("login.html")) {
        initLoginPage();
    } else if (pathname.endsWith("restaurant.html")) {
        initRestaurantPage();
    } else if (pathname.endsWith("cart.html")) {
        initCartPage();
    } else if (pathname.endsWith("dashboard.html")) {
        initDashboardPage();
    } else if (pathname.endsWith("admin.html")) {
        initAdminPage();
    }
});

// ==========================================
// Authentication State Management
// ==========================================

function getLoggedInUser() {
    const userStr = sessionStorage.getItem("quickbites_user");
    if (!userStr) return null;
    try {
        return JSON.parse(userStr);
    } catch (e) {
        return null;
    }
}

function setLoggedInUser(user) {
    if (user) {
        sessionStorage.setItem("quickbites_user", JSON.stringify(user));
    } else {
        sessionStorage.removeItem("quickbites_user");
    }
}

function checkAuth(allowedRoles) {
    const user = getLoggedInUser();
    if (!user) {
        window.location.href = "login.html";
        return null;
    }
    if (allowedRoles && !allowedRoles.includes(user.role)) {
        alert("Access Denied: You do not have permissions for this page.");
        window.location.href = "login.html";
        return null;
    }
    return user;
}

function logout() {
    setLoggedInUser(null);
    window.location.href = "login.html";
}

// ==========================================
// Shared UI Templates
// ==========================================

function renderNavbar() {
    const container = document.getElementById("navbar-container");
    if (!container) return;

    const user = getLoggedInUser();
    let navLinks = "";

    if (user) {
        if (user.role === "CUSTOMER") {
            navLinks = `
                <li class="nav-item"><a class="nav-link" href="index.html">Browse</a></li>
                <li class="nav-item"><a class="nav-link" href="dashboard.html">My Orders</a></li>
                <li class="nav-item"><a class="nav-link" href="cart.html">Cart <span class="badge bg-danger" id="cart-badge-count">0</span></a></li>
            `;
        } else if (user.role === "RESTAURANT") {
            navLinks = `
                <li class="nav-item"><a class="nav-link" href="dashboard.html">Store Dashboard</a></li>
            `;
        } else if (user.role === "DELIVERY") {
            navLinks = `
                <li class="nav-item"><a class="nav-link" href="dashboard.html">Deliveries</a></li>
            `;
        } else if (user.role === "ADMIN") {
            navLinks = `
                <li class="nav-item"><a class="nav-link" href="admin.html">Admin Console</a></li>
            `;
        }
    } else {
        navLinks = `
            <li class="nav-item"><a class="nav-link" href="index.html">Restaurants</a></li>
        `;
    }

    const authButton = user 
        ? `<div class="d-flex align-items-center gap-3">
             <span class="text-white opacity-75 small">Hi, <strong>${user.name || user.username}</strong> (${user.role})</span>
             <button class="btn btn-outline-light btn-sm px-3" onclick="logout()">Logout</button>
           </div>`
        : `<a class="btn btn-accent btn-sm px-4" href="login.html">Login</a>`;

    container.innerHTML = `
        <nav class="navbar navbar-expand-lg navbar-dark navbar-custom py-3">
            <div class="container">
                <a class="navbar-brand" href="index.html">Quick<span>Bites</span></a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarNav">
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                        ${navLinks}
                    </ul>
                    ${authButton}
                </div>
            </div>
        </nav>
    `;

    if (user && user.role === "CUSTOMER") {
        updateCartBadge(user.id);
    }
}

function updateCartBadge(userId) {
    fetch(`/api/cart?userId=${userId}`)
        .then(res => res.json())
        .then(data => {
            const badge = document.getElementById("cart-badge-count");
            if (badge) {
                let count = 0;
                if (data && data.items) {
                    data.items.forEach(i => count += i.quantity);
                }
                badge.innerText = count;
                badge.style.display = count > 0 ? "inline" : "none";
            }
        }).catch(err => console.error("Error updating cart badge:", err));
}

// ==========================================
// 1. Browse Page (index.html)
// ==========================================

function initBrowsePage() {
    loadRestaurantList("", "");

    // Search and filter listeners
    const searchBtn = document.getElementById("search-btn");
    const searchInput = document.getElementById("search-input");
    const sortSelect = document.getElementById("sort-select");

    if (searchBtn && searchInput) {
        searchBtn.addEventListener("click", () => {
            loadRestaurantList(searchInput.value, sortSelect.value);
        });
        searchInput.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                loadRestaurantList(searchInput.value, sortSelect.value);
            }
        });
    }

    if (sortSelect) {
        sortSelect.addEventListener("change", () => {
            loadRestaurantList(searchInput.value, sortSelect.value);
        });
    }
}

function loadRestaurantList(search, sortBy) {
    const listContainer = document.getElementById("restaurant-list");
    if (!listContainer) return;

    listContainer.innerHTML = `<div class="col-12 text-center py-5"><div class="spinner-border text-primary" role="status"></div></div>`;

    let url = "/api/restaurants";
    const params = [];
    if (search) params.push(`search=${encodeURIComponent(search)}`);
    if (sortBy) params.push(`sortBy=${encodeURIComponent(sortBy)}`);
    if (params.length > 0) url += "?" + params.join("&");

    fetch(url)
        .then(res => res.json())
        .then(data => {
            if (!data || data.length === 0) {
                listContainer.innerHTML = `
                    <div class="col-12 text-center py-5">
                        <h4 class="text-muted">No restaurants found matching your criteria.</h4>
                        <p class="text-secondary">Try searching for simple names like "Pizza" or "Burger"</p>
                    </div>`;
                return;
            }

            let html = "";
            data.forEach(r => {
                html += `
                    <div class="col-md-4 mb-4 animate-fade-in">
                        <div class="card-premium h-100 d-flex flex-column">
                            <div class="restaurant-img-placeholder">
                                ${r.name.split(" ")[0]}
                            </div>
                            <div class="card-body p-4 d-flex flex-column">
                                <h5 class="card-title fw-bold mb-1">${r.name}</h5>
                                <p class="text-secondary small mb-3">${r.cuisineType} Cuisine</p>
                                <div class="d-flex align-items-center justify-content-between mt-auto pt-3 border-top">
                                    <div class="d-flex gap-2">
                                        <span class="rating-badge">★ ${r.rating.toFixed(1)}</span>
                                        <span class="time-badge">🕒 ${r.deliveryTime} mins</span>
                                    </div>
                                    <a href="restaurant.html?id=${r.id}" class="btn btn-outline-custom btn-sm">View Menu</a>
                                </div>
                            </div>
                        </div>
                    </div>
                `;
            });
            listContainer.innerHTML = html;
        }).catch(err => {
            listContainer.innerHTML = `<div class="col-12 alert alert-danger">Error loading restaurants: ${err.message}</div>`;
        });
}

// ==========================================
// 2. Login Page (login.html)
// ==========================================

function initLoginPage() {
    const loginForm = document.getElementById("login-form");
    if (!loginForm) return;

    loginForm.addEventListener("submit", (e) => {
        e.preventDefault();
        
        const username = document.getElementById("login-username").value;
        const password = document.getElementById("login-password").value;
        
        // Active role tab selection
        const activeTabEl = document.querySelector("#roleTabs .nav-link.active");
        const role = activeTabEl.getAttribute("data-role");

        fetch("/api/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password, role })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                setLoggedInUser(data.user);
                
                // Route to respective dashboards
                if (role === "CUSTOMER") {
                    window.location.href = "index.html";
                } else if (role === "RESTAURANT" || role === "DELIVERY") {
                    window.location.href = "dashboard.html";
                } else if (role === "ADMIN") {
                    window.location.href = "admin.html";
                }
            } else {
                alert(data.message || "Login failed");
            }
        })
        .catch(err => {
            alert("Error during login request: " + err.message);
        });
    });
}

// ==========================================
// 3. Restaurant Details / Menu (restaurant.html)
// ==========================================

function initRestaurantPage() {
    const urlParams = new URLSearchParams(window.location.search);
    const id = urlParams.get("id");
    if (!id) {
        window.location.href = "index.html";
        return;
    }

    const restTitle = document.getElementById("restaurant-name");
    const restDetails = document.getElementById("restaurant-meta");
    const menuContainer = document.getElementById("menu-container");

    fetch(`/api/restaurant?id=${id}`)
        .then(res => res.json())
        .then(rest => {
            if (!rest || !rest.name) {
                alert("Restaurant not found!");
                window.location.href = "index.html";
                return;
            }

            restTitle.innerText = rest.name;
            restDetails.innerHTML = `
                <span class="badge bg-secondary p-2 me-2">${rest.cuisineType} Cuisine</span>
                <span class="rating-badge me-2">★ ${rest.rating.toFixed(1)}</span>
                <span class="time-badge">🕒 ${rest.deliveryTime} mins Delivery Time</span>
            `;

            if (!rest.menu || rest.menu.length === 0) {
                menuContainer.innerHTML = `<h5 class="text-muted text-center py-5">No menu items found.</h5>`;
                return;
            }

            let html = "";
            rest.menu.forEach(item => {
                html += `
                    <div class="col-md-6 mb-4">
                        <div class="card-premium p-4 h-100 d-flex flex-column justify-content-between">
                            <div>
                                <div class="d-flex justify-content-between align-items-start mb-2">
                                    <h5 class="fw-bold mb-0">${item.name}</h5>
                                    <span class="fw-bold text-accent font-monospace" style="color: var(--accent-color); font-size: 1.15rem;">
                                        $${item.price.toFixed(2)}
                                    </span>
                                </div>
                                <p class="text-secondary small mb-3">${item.description}</p>
                                <span class="badge bg-light text-dark mb-3">${item.category}</span>
                            </div>
                            <div>
                                <button class="btn btn-accent btn-sm w-100" onclick="addItemToCart(${item.id}, ${rest.id})">
                                    Add to Cart
                                </button>
                            </div>
                        </div>
                    </div>
                `;
            });
            menuContainer.innerHTML = html;
        }).catch(err => {
            alert("Error loading restaurant menu: " + err.message);
        });
}

function addItemToCart(itemId, restaurantId) {
    const user = getLoggedInUser();
    if (!user) {
        alert("You must be logged in as a Customer to add items to your cart!");
        window.location.href = "login.html";
        return;
    }
    if (user.role !== "CUSTOMER") {
        alert("Only customers can compile shopping carts.");
        return;
    }

    fetch("/api/cart/add", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId: user.id, itemId, quantity: 1, restaurantId })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            updateCartBadge(user.id);
            alert("Food item added to cart successfully!");
        } else {
            alert(data.message || "Failed to add to cart");
        }
    }).catch(err => {
        console.error("Cart error:", err);
    });
}

// ==========================================
// 4. Cart Page (cart.html)
// ==========================================

function initCartPage() {
    const user = checkAuth(["CUSTOMER"]);
    if (!user) return;

    loadCartDetails(user.id);

    // Coupon discount application
    const couponForm = document.getElementById("coupon-form");
    if (couponForm) {
        couponForm.addEventListener("submit", (e) => {
            e.preventDefault();
            loadCartDetails(user.id); // Re-calculates subtotal and applies the text value
        });
    }

    // Checkout button
    const checkoutBtn = document.getElementById("checkout-btn");
    if (checkoutBtn) {
        checkoutBtn.addEventListener("click", () => {
            const couponCode = document.getElementById("coupon-code").value.trim();
            
            fetch("/api/order/place", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ userId: user.id, couponCode })
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    alert("Order placed successfully!");
                    window.location.href = "dashboard.html";
                } else {
                    alert(data.message || "Failed to place order");
                }
            }).catch(err => alert("Checkout error: " + err.message));
        });
    }
}

function loadCartDetails(userId) {
    const cartContainer = document.getElementById("cart-items-container");
    const summaryCard = document.getElementById("cart-summary-card");
    const couponInput = document.getElementById("coupon-code");
    
    if (!cartContainer) return;

    fetch(`/api/cart?userId=${userId}`)
        .then(res => res.json())
        .then(cart => {
            if (!cart || !cart.items || cart.items.length === 0) {
                cartContainer.innerHTML = `<h5 class="text-muted py-5 text-center">Your shopping cart is empty!</h5>`;
                if (summaryCard) summaryCard.style.display = "none";
                return;
            }

            if (summaryCard) summaryCard.style.display = "block";

            let html = "";
            cart.items.forEach(ci => {
                html += `
                    <div class="card-premium p-3 mb-3 d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="fw-bold mb-1">${ci.foodItem.name}</h6>
                            <span class="text-secondary small">$${ci.foodItem.price.toFixed(2)} each</span>
                        </div>
                        <div class="d-flex align-items-center gap-3">
                            <div class="input-group input-group-sm" style="width: 100px;">
                                <button class="btn btn-outline-secondary" onclick="changeQty(${ci.foodItem.id}, ${ci.quantity - 1})">-</button>
                                <input type="text" class="form-control text-center" value="${ci.quantity}" readonly>
                                <button class="btn btn-outline-secondary" onclick="changeQty(${ci.foodItem.id}, ${ci.quantity + 1})">+</button>
                            </div>
                            <span class="fw-bold font-monospace">$${(ci.foodItem.price * ci.quantity).toFixed(2)}</span>
                            <button class="btn btn-sm text-danger border-0 bg-transparent" onclick="changeQty(${ci.foodItem.id}, 0)">
                                ✕ Remove
                            </button>
                        </div>
                    </div>
                `;
            });
            cartContainer.innerHTML = html;

            // Recalculate summary details
            const subtotal = cart.totalPrice;
            let couponApplied = couponInput ? couponInput.value.trim().toUpperCase() : "";
            let discount = 0;

            // Simple client side simulation of matching DataStore values
            if (couponApplied === "QUICK10") discount = Math.min(subtotal * 0.1, 5.0);
            if (couponApplied === "EATS20") discount = Math.min(subtotal * 0.2, 10.0);
            if (couponApplied === "BITE50") discount = Math.min(subtotal * 0.5, 15.0);

            const finalPrice = Math.max(0, subtotal - discount);

            document.getElementById("subtotal-val").innerText = `$${subtotal.toFixed(2)}`;
            
            const discountRow = document.getElementById("discount-row");
            const discountVal = document.getElementById("discount-val");
            if (discount > 0) {
                discountRow.style.display = "flex";
                discountVal.innerText = `-$${discount.toFixed(2)} (${couponApplied})`;
            } else {
                discountRow.style.display = "none";
            }

            document.getElementById("total-val").innerText = `$${finalPrice.toFixed(2)}`;
            updateCartBadge(userId);

        }).catch(err => {
            cartContainer.innerHTML = `<div class="alert alert-danger">Error retrieving cart: ${err.message}</div>`;
        });
}

function changeQty(itemId, quantity) {
    const user = getLoggedInUser();
    if (!user) return;

    fetch("/api/cart/update", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId: user.id, itemId, quantity })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            loadCartDetails(user.id);
        } else {
            alert(data.message || "Failed to update item quantity");
        }
    }).catch(err => console.error("Update quantity failed:", err));
}

// ==========================================
// 5. Dynamic Dashboards (dashboard.html)
// ==========================================

function initDashboardPage() {
    const user = checkAuth(["CUSTOMER", "RESTAURANT", "DELIVERY"]);
    if (!user) return;

    const pageTitle = document.getElementById("dashboard-title");
    if (pageTitle) pageTitle.innerText = `${user.role} Dashboard`;

    loadDashboardRoleContent(user);
}

function loadDashboardRoleContent(user) {
    const container = document.getElementById("dashboard-content");
    if (!container) return;

    if (user.role === "CUSTOMER") {
        renderCustomerDashboard(container, user);
    } else if (user.role === "RESTAURANT") {
        renderRestaurantDashboard(container, user);
    } else if (user.role === "DELIVERY") {
        renderDeliveryDashboard(container, user);
    }
}

// --- Customer Dashboard ---
function renderCustomerDashboard(container, user) {
    container.innerHTML = `
        <div class="row">
            <div class="col-md-4 mb-4">
                <div class="card-premium p-4">
                    <h5 class="fw-bold mb-3">Customer Profile</h5>
                    <p class="mb-1"><strong>Name:</strong> ${user.name}</p>
                    <p class="mb-1"><strong>Phone:</strong> ${user.phone}</p>
                    <p class="mb-0"><strong>Address:</strong> ${user.address}</p>
                </div>
            </div>
            <div class="col-md-8">
                <div class="card-premium p-4 mb-4" id="active-order-tracking">
                    <h5 class="fw-bold mb-3">Active Order Progress</h5>
                    <div id="live-tracker-placeholder">
                        <p class="text-secondary small">No active orders tracking at this time. Go ahead and place an order!</p>
                    </div>
                </div>
                <div class="card-premium p-4">
                    <h5 class="fw-bold mb-3">Order History (LIFO Stack)</h5>
                    <div id="customer-orders-history">
                        <div class="spinner-border text-primary spinner-border-sm" role="status"></div>
                    </div>
                </div>
            </div>
        </div>
    `;

    fetchOrdersAndRenderHistory(user.id, "CUSTOMER");
}

function fetchOrdersAndRenderHistory(userId, role) {
    fetch(`/api/orders?userId=${userId}&role=${role}`)
        .then(res => res.json())
        .then(orders => {
            const historyContainer = document.getElementById("customer-orders-history");
            const trackerPlaceholder = document.getElementById("live-tracker-placeholder");

            if (!orders || orders.length === 0) {
                if (historyContainer) historyContainer.innerHTML = `<p class="text-muted small">You haven't placed any orders yet.</p>`;
                return;
            }

            // Look for the first active (non-delivered, non-rejected) order for live tracking
            const activeOrder = orders.find(o => o.status !== "DELIVERED" && o.status !== "REJECTED");
            if (activeOrder && trackerPlaceholder) {
                let step = 1;
                if (activeOrder.status === "ACCEPTED") step = 2;
                if (activeOrder.status === "PREPARING") step = 3;
                if (activeOrder.status === "READY") step = 4;
                if (activeOrder.status === "ACCEPTED_DELIVERY") step = 5;
                if (activeOrder.status === "PICKED_UP") step = 6;

                const partnerText = activeOrder.deliveryPartnerId !== -1 
                    ? `<div class="alert alert-info py-2 small mt-3">Delivery Partner ID: <strong>#${activeOrder.deliveryPartnerId}</strong> has been assigned.</div>`
                    : `<div class="alert alert-warning py-2 small mt-3">Waiting for delivery partner assignment...</div>`;

                trackerPlaceholder.innerHTML = `
                    <div class="p-3 border rounded-3 bg-light animate-fade-in">
                        <div class="d-flex justify-content-between mb-2">
                            <span class="fw-bold">Order #${activeOrder.orderId}</span>
                            <span class="badge bg-warning text-dark font-monospace">${activeOrder.status}</span>
                        </div>
                        
                        <div class="progress-track mt-4 mb-2">
                            <div class="progress-step ${step >= 1 ? 'active' : ''}">1</div>
                            <div class="progress-step ${step >= 2 ? 'active' : ''}">2</div>
                            <div class="progress-step ${step >= 3 ? 'active' : ''}">3</div>
                            <div class="progress-step ${step >= 4 ? 'active' : ''}">4</div>
                            <div class="progress-step ${step >= 5 ? 'active' : ''}">5</div>
                            <div class="progress-step ${step >= 6 ? 'active' : ''}">6</div>
                        </div>
                        <div class="d-flex justify-content-between text-secondary px-1" style="font-size: 0.75rem;">
                            <span>Placed</span>
                            <span>Accepted</span>
                            <span>Cooking</span>
                            <span>Ready</span>
                            <span>Assigned</span>
                            <span>On the Way</span>
                        </div>
                        ${partnerText}
                    </div>
                `;
            } else if (trackerPlaceholder) {
                trackerPlaceholder.innerHTML = `<p class="text-success small fw-semibold">All orders completed! Standard Delivery services are ready.</p>`;
            }

            // Render History List
            if (historyContainer) {
                let html = "";
                orders.forEach(o => {
                    let statusBadge = `<span class="badge bg-secondary">${o.status}</span>`;
                    if (o.status === "DELIVERED") statusBadge = `<span class="badge bg-success">${o.status}</span>`;
                    if (o.status === "REJECTED") statusBadge = `<span class="badge bg-danger">${o.status}</span>`;

                    const dateStr = new Date(o.timestamp).toLocaleString();
                    html += `
                        <div class="border rounded-3 p-3 mb-3">
                            <div class="d-flex justify-content-between mb-1">
                                <span class="fw-bold">Order #${o.orderId}</span>
                                ${statusBadge}
                            </div>
                            <div class="text-secondary small mb-2">${dateStr}</div>
                            <div class="small">
                                ${o.items.map(i => `${i.foodItem.name} (${i.quantity})`).join(", ")}
                            </div>
                            <div class="d-flex justify-content-between align-items-center mt-2 pt-2 border-top">
                                <span class="small text-muted">${o.couponCode ? `Coupon Applied: ${o.couponCode}` : 'No Coupon'}</span>
                                <span class="fw-bold text-dark font-monospace">$${o.finalPrice.toFixed(2)}</span>
                            </div>
                        </div>
                    `;
                });
                historyContainer.innerHTML = html;
            }

        }).catch(err => {
            console.error("Error loading customer dashboard metrics:", err);
        });
}

// --- Restaurant Dashboard ---
function renderRestaurantDashboard(container, user) {
    container.innerHTML = `
        <div class="row">
            <div class="col-md-6 mb-4">
                <div class="card-premium p-4 h-100">
                    <h5 class="fw-bold mb-3 text-warning">Incoming Pending Queue</h5>
                    <div id="restaurant-pending-queue" style="max-height: 500px; overflow-y: auto;">
                        <div class="spinner-border text-warning spinner-border-sm" role="status"></div>
                    </div>
                </div>
            </div>
            <div class="col-md-6 mb-4">
                <div class="card-premium p-4 h-100">
                    <h5 class="fw-bold mb-3 text-info">In-Preparation / Ready Orders</h5>
                    <div id="restaurant-preparing-orders" style="max-height: 500px; overflow-y: auto;">
                        <div class="spinner-border text-info spinner-border-sm" role="status"></div>
                    </div>
                </div>
            </div>
        </div>
    `;

    loadRestaurantOrders(user.id);
}

function loadRestaurantOrders(restaurantId) {
    fetch(`/api/orders?userId=${restaurantId}&role=RESTAURANT`)
        .then(res => res.json())
        .then(orders => {
            const queueContainer = document.getElementById("restaurant-pending-queue");
            const prepContainer = document.getElementById("restaurant-preparing-orders");

            if (!orders || orders.length === 0) {
                if (queueContainer) queueContainer.innerHTML = `<p class="text-muted small">No new orders waiting.</p>`;
                if (prepContainer) prepContainer.innerHTML = `<p class="text-muted small">No active cooking operations.</p>`;
                return;
            }

            let pendingHtml = "";
            let prepHtml = "";

            orders.forEach(o => {
                const dateStr = new Date(o.timestamp).toLocaleTimeString();
                const itemSummary = o.items.map(i => `${i.foodItem.name} <strong>x${i.quantity}</strong>`).join("<br>");

                if (o.status === "PENDING") {
                    pendingHtml += `
                        <div class="border rounded-3 p-3 mb-3 bg-light">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="fw-bold">Order #${o.orderId}</span>
                                <span class="small text-muted font-monospace">${dateStr}</span>
                            </div>
                            <p class="small text-secondary mb-3">${itemSummary}</p>
                            <div class="d-flex gap-2">
                                <button class="btn btn-success btn-sm flex-fill" onclick="updateStatus(${o.orderId}, 'ACCEPTED')">Accept</button>
                                <button class="btn btn-danger btn-sm flex-fill" onclick="updateStatus(${o.orderId}, 'REJECTED')">Reject</button>
                            </div>
                        </div>
                    `;
                } else if (["ACCEPTED", "PREPARING", "READY", "ACCEPTED_DELIVERY", "PICKED_UP"].includes(o.status)) {
                    let actionBtn = "";
                    if (o.status === "ACCEPTED") {
                        actionBtn = `<button class="btn btn-primary btn-sm w-100" onclick="updateStatus(${o.orderId}, 'PREPARING')">Start Preparing</button>`;
                    } else if (o.status === "PREPARING") {
                        actionBtn = `<button class="btn btn-warning btn-sm w-100" onclick="updateStatus(${o.orderId}, 'READY')">Mark Ready (Assign Rider)</button>`;
                    } else if (o.status === "READY") {
                        actionBtn = `<span class="badge bg-secondary p-2 w-100">Waiting for Rider Pick up</span>`;
                    } else if (o.status === "ACCEPTED_DELIVERY") {
                        actionBtn = `<span class="badge bg-info p-2 w-100">Rider Commencing Pick up</span>`;
                    } else if (o.status === "PICKED_UP") {
                        actionBtn = `<span class="badge bg-success p-2 w-100">Out for Delivery</span>`;
                    }

                    prepHtml += `
                        <div class="border rounded-3 p-3 mb-3 bg-white">
                            <div class="d-flex justify-content-between mb-2">
                                <span class="fw-bold">Order #${o.orderId}</span>
                                <span class="badge bg-info font-monospace">${o.status}</span>
                            </div>
                            <p class="small text-secondary mb-3">${itemSummary}</p>
                            ${actionBtn}
                        </div>
                    `;
                }
            });

            if (queueContainer) {
                queueContainer.innerHTML = pendingHtml || `<p class="text-muted small">No new orders in the queue.</p>`;
            }
            if (prepContainer) {
                prepContainer.innerHTML = prepHtml || `<p class="text-muted small">No active items in kitchen.</p>`;
            }

        }).catch(err => console.error("Error loading restaurant panel:", err));
}

// --- Delivery Partner Dashboard ---
function renderDeliveryDashboard(container, user) {
    container.innerHTML = `
        <div class="row">
            <div class="col-md-5 mb-4">
                <div class="card-premium p-4">
                    <h5 class="fw-bold mb-3">Rider Status</h5>
                    <p class="mb-1"><strong>Name:</strong> ${user.name}</p>
                    <p class="mb-1"><strong>Rating:</strong> ★ ${user.rating.toFixed(1)}</p>
                    <p class="mb-0"><strong>Availability:</strong> 
                        <span class="badge ${user.isAvailable ? 'bg-success' : 'bg-danger'}">
                            ${user.isAvailable ? 'Available' : 'Busy'}
                        </span>
                    </p>
                </div>
            </div>
            <div class="col-md-7">
                <div class="card-premium p-4">
                    <h5 class="fw-bold mb-3">Assigned Orders</h5>
                    <div id="assigned-deliveries">
                        <div class="spinner-border text-primary spinner-border-sm" role="status"></div>
                    </div>
                </div>
            </div>
        </div>
    `;

    loadAssignedDeliveries(user.id);
}

function loadAssignedDeliveries(partnerId) {
    fetch(`/api/orders?userId=${partnerId}&role=DELIVERY`)
        .then(res => res.json())
        .then(orders => {
            const container = document.getElementById("assigned-deliveries");
            if (!container) return;

            // Only show active assigned orders (not yet delivered or rejected)
            const activeDeliveries = orders.filter(o => o.status !== "DELIVERED" && o.status !== "REJECTED");

            if (!activeDeliveries || activeDeliveries.length === 0) {
                container.innerHTML = `<p class="text-muted small">No active deliveries assigned to you at the moment.</p>`;
                return;
            }

            let html = "";
            activeDeliveries.forEach(o => {
                let actionHtml = "";
                if (o.status === "READY") {
                    actionHtml = `<button class="btn btn-success btn-sm w-100" onclick="updateStatus(${o.orderId}, 'ACCEPTED_DELIVERY')">Accept Delivery</button>`;
                } else if (o.status === "ACCEPTED_DELIVERY") {
                    actionHtml = `<button class="btn btn-warning btn-sm w-100" onclick="updateStatus(${o.orderId}, 'PICKED_UP')">Mark Picked Up</button>`;
                } else if (o.status === "PICKED_UP") {
                    actionHtml = `<button class="btn btn-primary btn-sm w-100" onclick="updateStatus(${o.orderId}, 'DELIVERED')">Mark Delivered</button>`;
                }

                const dateStr = new Date(o.timestamp).toLocaleTimeString();
                html += `
                    <div class="border rounded-3 p-3 bg-light mb-3">
                        <div class="d-flex justify-content-between mb-2">
                            <span class="fw-bold">Delivery Order #${o.orderId}</span>
                            <span class="badge bg-warning text-dark font-monospace">${o.status}</span>
                        </div>
                        <div class="small text-secondary mb-3">
                            <strong>Restaurant ID:</strong> #${o.restaurantId}<br>
                            <strong>Price Value:</strong> $${o.finalPrice.toFixed(2)}<br>
                            <strong>Assigned At:</strong> ${dateStr}
                        </div>
                        ${actionHtml}
                    </div>
                `;
            });
            container.innerHTML = html;
        }).catch(err => console.error("Error loading delivery portal:", err));
}

function updateStatus(orderId, nextStatus) {
    const user = getLoggedInUser();
    if (!user) return;

    fetch("/api/order/status", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ orderId, status: nextStatus, userId: user.id })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            // Hot update local user object in session storage if rider status changes
            if (user.role === "DELIVERY" && nextStatus === "DELIVERED") {
                user.isAvailable = true;
                user.currentOrderId = -1;
                setLoggedInUser(user);
            }
            if (user.role === "DELIVERY" && nextStatus === "ACCEPTED_DELIVERY") {
                user.isAvailable = false;
                user.currentOrderId = orderId;
                setLoggedInUser(user);
            }

            renderNavbar(); // Refresh badge
            loadDashboardRoleContent(user);
        } else {
            alert(data.message || "Failed to update order status");
        }
    }).catch(err => alert("Error updating status: " + err.message));
}

// ==========================================
// 6. Admin Panel (admin.html)
// ==========================================

function initAdminPage() {
    checkAuth(["ADMIN"]);
    loadAdminDashboard();
}

function loadAdminDashboard() {
    const statsContainer = document.getElementById("admin-stats");
    const activeOrdersTable = document.getElementById("active-orders-tbody");

    if (!statsContainer) return;

    fetch("/api/admin/stats")
        .then(res => res.json())
        .then(stats => {
            statsContainer.innerHTML = `
                <div class="row">
                    <div class="col-md-4 col-sm-6 mb-4">
                        <div class="stat-box animate-fade-in">
                            <div class="stat-val">${stats.totalRestaurants}</div>
                            <div class="stat-label">Restaurants</div>
                        </div>
                    </div>
                    <div class="col-md-4 col-sm-6 mb-4">
                        <div class="stat-box animate-fade-in">
                            <div class="stat-val">${stats.totalCustomers}</div>
                            <div class="stat-label">Customers</div>
                        </div>
                    </div>
                    <div class="col-md-4 col-sm-6 mb-4">
                        <div class="stat-box animate-fade-in">
                            <div class="stat-val">${stats.totalOrders}</div>
                            <div class="stat-label">Total Orders</div>
                        </div>
                    </div>
                    <div class="col-md-4 col-sm-6 mb-4">
                        <div class="stat-box animate-fade-in">
                            <div class="stat-val text-warning">${stats.activeOrders}</div>
                            <div class="stat-label">Active Orders</div>
                        </div>
                    </div>
                    <div class="col-md-4 col-sm-6 mb-4">
                        <div class="stat-box animate-fade-in">
                            <div class="stat-val text-success">${stats.deliveredOrders}</div>
                            <div class="stat-label">Delivered Orders</div>
                        </div>
                    </div>
                    <div class="col-md-4 col-sm-6 mb-4">
                        <div class="stat-box animate-fade-in">
                            <div class="stat-val text-info">${stats.availableDeliverersCount}</div>
                            <div class="stat-label">Available Riders</div>
                        </div>
                    </div>
                </div>
            `;
        }).catch(err => console.error("Error loading admin stats:", err));

    fetch("/api/orders?userId=1001&role=ADMIN")
        .then(res => res.json())
        .then(orders => {
            if (!activeOrdersTable) return;

            if (!orders || orders.length === 0) {
                activeOrdersTable.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No orders found in database.</td></tr>`;
                return;
            }

            let html = "";
            orders.forEach(o => {
                const dateStr = new Date(o.timestamp).toLocaleString();
                let statusBadge = `<span class="badge bg-secondary">${o.status}</span>`;
                if (o.status === "DELIVERED") statusBadge = `<span class="badge bg-success">${o.status}</span>`;
                if (o.status === "PENDING") statusBadge = `<span class="badge bg-warning text-dark">${o.status}</span>`;
                if (o.status === "READY") statusBadge = `<span class="badge bg-info">${o.status}</span>`;
                if (o.status === "REJECTED") statusBadge = `<span class="badge bg-danger">${o.status}</span>`;

                html += `
                    <tr>
                        <td class="fw-semibold">#${o.orderId}</td>
                        <td>Cust #${o.customerId}</td>
                        <td>Rest #${o.restaurantId}</td>
                        <td class="small">${o.items.map(i => `${i.foodItem.name} (${i.quantity})`).join(", ")}</td>
                        <td class="font-monospace fw-bold">$${o.finalPrice.toFixed(2)}</td>
                        <td>${statusBadge}</td>
                    </tr>
                `;
            });
            activeOrdersTable.innerHTML = html;
        }).catch(err => {
            if (activeOrdersTable) activeOrdersTable.innerHTML = `<tr><td colspan="6" class="text-danger">Error loading active table: ${err.message}</td></tr>`;
        });
}
