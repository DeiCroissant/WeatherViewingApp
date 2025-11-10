package com.example.weatherviewingapp;

/**
 * Location entity for database storage
 * Represents a saved city/location
 */
public class Location {
    private int id;
    private String cityName;
    private String countryCode;
    private double latitude;
    private double longitude;
    private String tag; // "Home", "Work", or empty
    private boolean isDefault; // Default location to show on startup
    private int sortOrder; // For custom ordering
    private long lastUpdated; // Timestamp of last weather fetch
    
    public Location() {
    }
    
    public Location(String cityName, String countryCode, double latitude, double longitude) {
        this.cityName = cityName;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tag = "";
        this.isDefault = false;
        this.sortOrder = 0;
        this.lastUpdated = 0;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getCityName() {
        return cityName;
    }
    
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public int getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Get display name with tag if exists
     */
    public String getDisplayName() {
        if (tag != null && !tag.isEmpty()) {
            return cityName + " (" + tag + ")";
        }
        return cityName;
    }
    
    /**
     * Get full name with country
     */
    public String getFullName() {
        if (countryCode != null && !countryCode.isEmpty()) {
            return cityName + ", " + countryCode;
        }
        return cityName;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
