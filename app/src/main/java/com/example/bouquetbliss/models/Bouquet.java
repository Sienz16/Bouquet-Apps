package com.example.bouquetbliss.models;

import java.util.List;

public class Bouquet {
    private String id;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String category;
    private List<String> imageUrls;
    private String availability; // "Available", "Out of Stock", "Limited"
    private List<String> flowerTypes;
    private String size; // "Small", "Medium", "Large"
    private String occasion; // "Wedding", "Birthday", "Anniversary", etc.
    private long createdAt;
    private long updatedAt;
    private String createdBy; // Admin/Technician ID
    
    // Rating related fields
    private double averageRating; // Average rating from all reviews
    private int totalRatings; // Total number of ratings received
    
    // Default constructor required for Firebase
    public Bouquet() {}
    
    public Bouquet(String name, String description, double price, int quantity, 
                   String category, List<String> imageUrls, String size, String occasion) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.imageUrls = imageUrls;
        this.size = size;
        this.occasion = occasion;
        this.availability = quantity > 0 ? "Available" : "Out of Stock";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        
        // Initialize rating fields
        this.averageRating = 0.0;
        this.totalRatings = 0;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getCategory() { return category; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getAvailability() { return availability; }
    public List<String> getFlowerTypes() { return flowerTypes; }
    public String getSize() { return size; }
    public String getOccasion() { return occasion; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public double getAverageRating() { return averageRating; }
    public int getTotalRatings() { return totalRatings; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        updateAvailability();
    }
    public void setCategory(String category) { this.category = category; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public void setAvailability(String availability) { this.availability = availability; }
    public void setFlowerTypes(List<String> flowerTypes) { this.flowerTypes = flowerTypes; }
    public void setSize(String size) { this.size = size; }
    public void setOccasion(String occasion) { this.occasion = occasion; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }
    
    // Helper methods
    private void updateAvailability() {
        if (quantity <= 0) {
            this.availability = "Out of Stock";
        } else if (quantity <= 5) {
            this.availability = "Limited";
        } else {
            this.availability = "Available";
        }
        this.updatedAt = System.currentTimeMillis();
    }
    
    public void updateStock(int newQuantity) {
        setQuantity(newQuantity);
    }
    
    public boolean isAvailable() {
        return quantity > 0;
    }
    
    // Rating helper methods
    public void updateRating(double newAverageRating, int newTotalRatings) {
        this.averageRating = newAverageRating;
        this.totalRatings = newTotalRatings;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public String getFormattedRating() {
        if (totalRatings == 0) {
            return "No ratings yet";
        }
        return String.format("%.1f ★ (%d reviews)", averageRating, totalRatings);
    }
    
    public String getStarsDisplay() {
        StringBuilder stars = new StringBuilder();
        int fullStars = (int) averageRating;
        boolean hasHalfStar = (averageRating - fullStars) >= 0.5;
        
        for (int i = 1; i <= 5; i++) {
            if (i <= fullStars) {
                stars.append("★");
            } else if (i == fullStars + 1 && hasHalfStar) {
                stars.append("⯪");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    // Predefined categories
    public static class Categories {
        public static final String WEDDING = "Wedding";
        public static final String BIRTHDAY = "Birthday"; 
        public static final String ANNIVERSARY = "Anniversary";
        public static final String ROMANTIC = "Romantic";
        public static final String SYMPATHY = "Sympathy";
        public static final String CONGRATULATIONS = "Congratulations";
        public static final String SEASONAL = "Seasonal";
        public static final String CUSTOM = "Custom";
        
        public static String[] getAllCategories() {
            return new String[]{WEDDING, BIRTHDAY, ANNIVERSARY, ROMANTIC, 
                              SYMPATHY, CONGRATULATIONS, SEASONAL, CUSTOM};
        }
    }
    
    // Predefined sizes
    public static class Sizes {
        public static final String SMALL = "Small";
        public static final String MEDIUM = "Medium";
        public static final String LARGE = "Large";
        public static final String EXTRA_LARGE = "Extra Large";
        
        public static String[] getAllSizes() {
            return new String[]{SMALL, MEDIUM, LARGE, EXTRA_LARGE};
        }
    }
}
