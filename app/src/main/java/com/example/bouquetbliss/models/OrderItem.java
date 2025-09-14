package com.example.bouquetbliss.models;

public class OrderItem {
    private String bouquetId;
    private String bouquetName;
    private String bouquetImageUrl;
    private double unitPrice;
    private int quantity;
    private double subtotal;

    // Constructors
    public OrderItem() {}

    public OrderItem(String bouquetId, String bouquetName, String bouquetImageUrl, 
                     double unitPrice, int quantity) {
        this.bouquetId = bouquetId;
        this.bouquetName = bouquetName;
        this.bouquetImageUrl = bouquetImageUrl;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice * quantity;
    }

    // Getters and Setters
    public String getBouquetId() { return bouquetId; }
    public void setBouquetId(String bouquetId) { this.bouquetId = bouquetId; }

    public String getBouquetName() { return bouquetName; }
    public void setBouquetName(String bouquetName) { this.bouquetName = bouquetName; }

    public String getBouquetImageUrl() { return bouquetImageUrl; }
    public void setBouquetImageUrl(String bouquetImageUrl) { this.bouquetImageUrl = bouquetImageUrl; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice * quantity;
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        this.subtotal = unitPrice * quantity;
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    // Helper methods
    public String getFormattedUnitPrice() {
        return String.format("RM %.2f", unitPrice);
    }

    public String getFormattedSubtotal() {
        return String.format("RM %.2f", subtotal);
    }
}
