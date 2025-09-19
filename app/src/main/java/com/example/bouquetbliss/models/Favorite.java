package com.example.bouquetbliss.models;

public class Favorite {
    private String id;
    private String userId;
    private String bouquetId;
    private long createdAt;
    
    // Default constructor required for Firebase
    public Favorite() {}
    
    public Favorite(String userId, String bouquetId) {
        this.userId = userId;
        this.bouquetId = bouquetId;
        this.createdAt = System.currentTimeMillis();
    }
    
    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getBouquetId() { return bouquetId; }
    public long getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setBouquetId(String bouquetId) { this.bouquetId = bouquetId; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
