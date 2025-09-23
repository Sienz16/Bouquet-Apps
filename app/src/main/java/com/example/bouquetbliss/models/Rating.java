package com.example.bouquetbliss.models;

public class Rating {
    private String id;
    private String orderId;
    private String customerId;
    private String customerName;
    private String bouquetId;
    private String bouquetName;
    private int stars; // 1-5 stars
    private String comment;
    private long ratedAt;
    private boolean isVerifiedPurchase;

    // Default constructor required for Firebase
    public Rating() {}

    public Rating(String orderId, String customerId, String customerName, 
                  String bouquetId, String bouquetName, int stars, String comment) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.bouquetId = bouquetId;
        this.bouquetName = bouquetName;
        this.stars = stars;
        this.comment = comment;
        this.ratedAt = System.currentTimeMillis();
        this.isVerifiedPurchase = true; // Since it's from completed order
    }

    // Getters
    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getBouquetId() { return bouquetId; }
    public String getBouquetName() { return bouquetName; }
    public int getStars() { return stars; }
    public String getComment() { return comment; }
    public long getRatedAt() { return ratedAt; }
    public boolean isVerifiedPurchase() { return isVerifiedPurchase; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setBouquetId(String bouquetId) { this.bouquetId = bouquetId; }
    public void setBouquetName(String bouquetName) { this.bouquetName = bouquetName; }
    public void setStars(int stars) { this.stars = stars; }
    public void setComment(String comment) { this.comment = comment; }
    public void setRatedAt(long ratedAt) { this.ratedAt = ratedAt; }
    public void setVerifiedPurchase(boolean verifiedPurchase) { this.isVerifiedPurchase = verifiedPurchase; }

    // Helper methods
    public boolean isValidRating() {
        return stars >= 1 && stars <= 5;
    }

    public String getStarsDisplay() {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= this.stars) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
}
