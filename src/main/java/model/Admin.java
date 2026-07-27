package model;

/**
 * Represents an Admin in the QuickBites system.
 * Inherits from User. Admins can view analytics on restaurants, customers, and orders.
 */
public class Admin extends User {
    
    public Admin(int id, String username, String password) {
        super(id, username, password, "ADMIN");
    }
}
