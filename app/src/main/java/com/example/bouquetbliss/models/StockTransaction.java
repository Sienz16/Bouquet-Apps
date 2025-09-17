package com.example.bouquetbliss.models;

import java.util.Date;

/**
 * Model for tracking stock transactions and movements
 */
public class StockTransaction {
    private String id;
    private String bouquetId;
    private String bouquetName;
    private TransactionType type;
    private int quantityChange;
    private int previousQuantity;
    private int newQuantity;
    private String reason;
    private String adminId;
    private String adminName;
    private long timestamp;
    private String orderId; // For transactions related to orders
    
    // Default constructor for Firebase
    public StockTransaction() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public StockTransaction(String bouquetId, String bouquetName, TransactionType type, 
                          int quantityChange, int previousQuantity, int newQuantity, 
                          String reason, String adminId, String adminName) {
        this();
        this.bouquetId = bouquetId;
        this.bouquetName = bouquetName;
        this.type = type;
        this.quantityChange = quantityChange;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.reason = reason;
        this.adminId = adminId;
        this.adminName = adminName;
    }
    
    public enum TransactionType {
        STOCK_IN("Stock In"),
        STOCK_OUT("Stock Out"),
        ADJUSTMENT("Adjustment"),
        ORDER_FULFILLMENT("Order Fulfillment"),
        DAMAGED("Damaged/Lost"),
        EXPIRED("Expired");
        
        private final String displayName;
        
        TransactionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static TransactionType fromString(String value) {
            for (TransactionType type : TransactionType.values()) {
                if (type.name().equalsIgnoreCase(value) || 
                    type.displayName.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return ADJUSTMENT;
        }
    }
    
    // Getters
    public String getId() { return id; }
    public String getBouquetId() { return bouquetId; }
    public String getBouquetName() { return bouquetName; }
    public TransactionType getType() { return type; }
    public int getQuantityChange() { return quantityChange; }
    public int getPreviousQuantity() { return previousQuantity; }
    public int getNewQuantity() { return newQuantity; }
    public String getReason() { return reason; }
    public String getAdminId() { return adminId; }
    public String getAdminName() { return adminName; }
    public long getTimestamp() { return timestamp; }
    public String getOrderId() { return orderId; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setBouquetId(String bouquetId) { this.bouquetId = bouquetId; }
    public void setBouquetName(String bouquetName) { this.bouquetName = bouquetName; }
    public void setType(TransactionType type) { this.type = type; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }
    public void setPreviousQuantity(int previousQuantity) { this.previousQuantity = previousQuantity; }
    public void setNewQuantity(int newQuantity) { this.newQuantity = newQuantity; }
    public void setReason(String reason) { this.reason = reason; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public void setAdminName(String adminName) { this.adminName = adminName; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    // Helper methods
    public String getFormattedTimestamp() {
        return new Date(timestamp).toString();
    }
    
    public boolean isStockIncrease() {
        return quantityChange > 0;
    }
    
    public boolean isStockDecrease() {
        return quantityChange < 0;
    }
    
    public String getTransactionSummary() {
        String direction = isStockIncrease() ? "+" : "";
        return String.format("%s: %s%d units (%d → %d)", 
                           type.getDisplayName(), 
                           direction, 
                           quantityChange, 
                           previousQuantity, 
                           newQuantity);
    }
}
