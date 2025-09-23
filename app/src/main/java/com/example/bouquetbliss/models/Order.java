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
    private String flowerColorPreference; // User's preferred flower colors
    private String rejectionReason;
    private long createdAt;
    private long updatedAt;
    
    // Payment related fields
    private String paymentStatus; // pending, processing, paid, failed, refunded
    private String paymentMethod; // toyyibpay, cash_on_delivery
    private String transactionId; // ToyyibPay transaction ID
    private String billCode; // ToyyibPay bill code
    private String paymentUrl; // ToyyibPay payment URL
    private double paidAmount;
    private long paymentDate;
    private String invoicePath; // Path to generated PDF invoice
    
    // Rating related fields
    private boolean isRated; // Whether user has rated this order
    private long ratedAt; // When the order was rated
    
    // Delivery confirmation fields
    private long deliveryConfirmedAt; // When customer confirmed delivery
    private String deliveryConfirmedBy; // Who confirmed delivery (customer/admin)

    // Constructors
    public Order() {}

    public Order(String customerId, String customerName, String customerPhone, String customerEmail,
                 List<OrderItem> items, double totalAmount, long deliveryDate, String deliveryAddress, 
                 String specialInstructions, String flowerColorPreference) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.items = items;
        this.totalAmount = totalAmount;
        this.deliveryDate = deliveryDate;
        this.deliveryAddress = deliveryAddress;
        this.specialInstructions = specialInstructions;
        this.flowerColorPreference = flowerColorPreference;
        this.status = "pending";
        this.preparationStatus = "not_started";
        this.orderDate = System.currentTimeMillis();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        
        // Initialize payment fields
        this.paymentStatus = PaymentStatus.PENDING;
        this.paymentMethod = "";
        this.paidAmount = 0.0;
        this.paymentDate = 0;
        
        // Initialize rating fields
        this.isRated = false;
        this.ratedAt = 0;
        
        // Initialize delivery confirmation fields
        this.deliveryConfirmedAt = 0;
        this.deliveryConfirmedBy = "";
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

    public static class PaymentStatus {
        public static final String PENDING = "pending";
        public static final String PROCESSING = "processing";
        public static final String PAID = "paid";
        public static final String FAILED = "failed";
        public static final String REFUNDED = "refunded";
    }

    public static class PaymentMethod {
        public static final String TOYYIBPAY = "toyyibpay";
        public static final String CASH_ON_DELIVERY = "cash_on_delivery";
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

    public String getFlowerColorPreference() { return flowerColorPreference; }
    public void setFlowerColorPreference(String flowerColorPreference) { this.flowerColorPreference = flowerColorPreference; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Payment related getters and setters
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { 
        this.paymentStatus = paymentStatus;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getBillCode() { return billCode; }
    public void setBillCode(String billCode) { this.billCode = billCode; }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public long getPaymentDate() { return paymentDate; }
    public void setPaymentDate(long paymentDate) { this.paymentDate = paymentDate; }

    public String getInvoicePath() { return invoicePath; }
    public void setInvoicePath(String invoicePath) { this.invoicePath = invoicePath; }
    
    // Rating related getters and setters
    public boolean isRated() { return isRated; }
    public void setRated(boolean rated) { 
        this.isRated = rated;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public long getRatedAt() { return ratedAt; }
    public void setRatedAt(long ratedAt) { this.ratedAt = ratedAt; }
    
    // Delivery confirmation getters and setters
    public long getDeliveryConfirmedAt() { return deliveryConfirmedAt; }
    public void setDeliveryConfirmedAt(long deliveryConfirmedAt) { this.deliveryConfirmedAt = deliveryConfirmedAt; }
    
    public String getDeliveryConfirmedBy() { return deliveryConfirmedBy; }
    public void setDeliveryConfirmedBy(String deliveryConfirmedBy) { this.deliveryConfirmedBy = deliveryConfirmedBy; }

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

    // Payment helper methods
    public boolean isPaymentPending() { return PaymentStatus.PENDING.equals(paymentStatus); }
    public boolean isPaymentProcessing() { return PaymentStatus.PROCESSING.equals(paymentStatus); }
    public boolean isPaymentPaid() { return PaymentStatus.PAID.equals(paymentStatus); }
    public boolean isPaymentFailed() { return PaymentStatus.FAILED.equals(paymentStatus); }
    public boolean hasPaymentUrl() { return paymentUrl != null && !paymentUrl.isEmpty(); }
    
    // Rating helper methods
    public boolean canBeRated() { 
        return isCompleted() && isPaymentPaid() && !isRated(); 
    }
    
    public void markAsRated() {
        this.isRated = true;
        this.ratedAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    
    // Delivery confirmation helper methods
    public boolean isDeliveryConfirmed() {
        return deliveryConfirmedAt > 0;
    }
    
    public boolean isDeliveryConfirmedByCustomer() {
        return "customer".equals(deliveryConfirmedBy);
    }
}
