package com.crm.poc.user.model;

public class User {

    private String id;
    private String name;
    private String username;
    private String miscode;
    private UserRole role;
    private String managerId;

    public User() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMiscode() { return miscode; }
    public void setMiscode(String miscode) { this.miscode = miscode; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
}
