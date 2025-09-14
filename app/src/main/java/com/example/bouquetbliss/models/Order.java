package com.example.bouquetbliss.models;

import java.util.List;

public class Order {
    private String id;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private List<OrderItem> items;
    private double totalAmount;
    private String status; // pending, approved, in_preparation, ready, completed, rejected
    private String preparationStatus; // not_started, preparing, ready_for_pickup, completed
    private long orderDate;
    private long deliveryDate;
    private String deliveryAddress;
    private String specialInstructions;
    private String rejectionReason;
    private long createdAt;
    private long updatedAt;

    // Constructors
    public Order() {}

    public Order(String customerId, String customerName, String customerPhone, String customerEmail,
                 List<OrderItem> items, double totalAmount, long deliveryDate, String deliveryAddress) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.items = items;
        this.totalAmount = totalAmount;
        this.deliveryDate = deliveryDate;
        this.deliveryAddress = deliveryAddress;
        this.status = "pending";
        this.preparationStatus = "not_started";
        this.orderDate = System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Status constants
    public static class Status {
        public static final String PENDING = "pending";
        public static final String APPROVED = "approved";
        public static final String IN_PREPARATION = "in_preparation";
        public static final String READY = "ready";
        public static final String COMPLETED = "completed";
        public static final String REJECTED = "rejected";
    }

    public static class PreparationStatus {
        public static final String NOT_STARTED = "not_started";
        public static final String PREPARING = "preparing";
        public static final String READY_FOR_PICKUP = "ready_for_pickup";
        public static final String COMPLETED = "completed";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getPreparationStatus() { return preparationStatus; }
    public void setPreparationStatus(String preparationStatus) { 
        this.preparationStatus = preparationStatus;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getOrderDate() { return orderDate; }
    public void setOrderDate(long orderDate) { this.orderDate = orderDate; }

    public long getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(long deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isPending() { return Status.PENDING.equals(status); }
    public boolean isApproved() { return Status.APPROVED.equals(status) || Status.IN_PREPARATION.equals(status); }
    public boolean isCompleted() { return Status.COMPLETED.equals(status); }
    public boolean isRejected() { return Status.REJECTED.equals(status); }

    public String getFormattedOrderId() {
        if (id == null) {
            return "BB000";
        }
        
        // Extract numeric part from the ID
        String numericPart = id.replaceAll("[^0-9]", "");
        
        if (numericPart.isEmpty()) {
            return "BB000";
        }
        
        try {
            int orderNumber = Integer.parseInt(numericPart);
            return "BB" + String.format("%03d", orderNumber);
        } catch (NumberFormatException e) {
            // Fallback: use hash code of the ID
            int hashCode = Math.abs(id.hashCode()) % 1000;
            return "BB" + String.format("%03d", hashCode);
        }
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
}
