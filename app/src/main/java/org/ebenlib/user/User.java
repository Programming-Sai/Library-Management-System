package org.ebenlib.user;

public class User {
    private final String username;
    private final String password;
    private String role;
    private boolean active;

    public User(String username, String password, String role, boolean active) {
        this.username = username;
        this.role = role;
        this.active = active;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }

    public void setRole(String role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return username + " (" + role + ")" + ", Account Status: " + (this.active == true ? "Activated" : "Deactivated");
    }
}
