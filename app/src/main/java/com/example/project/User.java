package com.example.project;

public class User {
    private String username;
    private String name;

    // Constructor mặc định (rỗng) cần thiết cho Firebase
    public User() {
        // Do nothing
    }

    // Constructor chấp nhận username và name
    public User(String username, String name) {
        this.username = username;
        this.name = name;
    }

    // Getters và setters cho các trường dữ liệu
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
