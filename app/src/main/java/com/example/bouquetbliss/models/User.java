package com.example.bouquetbliss.models;

public class User {
    public enum UserType {
        ADMIN("admin"),
        USER("user");
        
        private final String value;
        
        UserType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static UserType fromString(String value) {
            for (UserType type : UserType.values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return USER; // Default to USER if not found
        }
    }
    
    private String uid;
    private String email;
    private String fullName;
    private String phoneNumber;
    private UserType userType;
    private String profileImageUrl;
    
    // Address fields
    private String streetAddress;
    private String city;
    private String state; // Only "Penang" or "Kedah"
    private String postalCode;
    
    private long createdAt;
    private long updatedAt;
    
    // Default constructor required for Firestore
    public User() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.userType = UserType.USER; // Default role
    }
    
    public User(String uid, String email, String fullName, UserType userType) {
        this();
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.userType = userType;
    }
    
    // Getters and setters
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public UserType getUserType() {
        return userType;
    }
    
    public void setUserType(UserType userType) {
        this.userType = userType;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Address getters and setters
    public String getStreetAddress() {
        return streetAddress;
    }
    
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
        this.updatedAt = System.currentTimeMillis();
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        // Only allow Penang or Kedah
        if ("Penang".equals(state) || "Kedah".equals(state)) {
            this.state = state;
            this.updatedAt = System.currentTimeMillis();
        }
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        this.updatedAt = System.currentTimeMillis();
    }
    
    // Helper methods
    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }
    
    public boolean isRegularUser() {
        return userType == UserType.USER;
    }
    
    public String getFormattedAddress() {
        StringBuilder address = new StringBuilder();
        
        if (streetAddress != null && !streetAddress.trim().isEmpty()) {
            address.append(streetAddress);
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            if (address.length() > 0) address.append(" ");
            address.append(postalCode);
        }
        
        if (state != null && !state.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(state);
        }
        
        return address.length() > 0 ? address.toString() : "No address set";
    }
    
    public boolean hasCompleteAddress() {
        return streetAddress != null && !streetAddress.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               state != null && !state.trim().isEmpty() &&
               postalCode != null && !postalCode.trim().isEmpty();
    }
    
    // State constants
    public static class States {
        public static final String PENANG = "Penang";
        public static final String KEDAH = "Kedah";
        
        public static String[] getAllStates() {
            return new String[]{PENANG, KEDAH};
        }
    }
    
    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", userType=" + userType +
                '}';
    }
}
