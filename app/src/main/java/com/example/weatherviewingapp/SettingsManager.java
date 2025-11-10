package com.example.weatherviewingapp;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class to manage SharedPreferences (Ch. 7.a)
 * Stores: Temperature unit (C/F), default city, cached weather data, last update time
 */
public class SettingsManager {
    private static final String PREF_NAME = "WeatherAppPreferences";
    private static final String KEY_TEMPERATURE_UNIT = "temperature_unit";
    private static final String KEY_DEFAULT_CITY = "default_city";
    private static final String KEY_CACHED_TEMP = "cached_temp";
    private static final String KEY_CACHED_CONDITION = "cached_condition";
    private static final String KEY_CACHED_CITY = "cached_city";
    private static final String KEY_LAST_UPDATE_TIME = "last_update_time";
    
    // Temperature units
    public static final String UNIT_CELSIUS = "C";
    public static final String UNIT_FAHRENHEIT = "F";
    
    private SharedPreferences sharedPreferences;
    
    public SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    // ==================== Temperature Unit ====================
    
    /**
     * Set temperature unit (C or F)
     */
    public void setTemperatureUnit(String unit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TEMPERATURE_UNIT, unit);
        editor.apply();
    }
    
    /**
     * Get temperature unit (default: Celsius)
     */
    public String getTemperatureUnit() {
        return sharedPreferences.getString(KEY_TEMPERATURE_UNIT, UNIT_CELSIUS);
    }
    
    /**
     * Check if using Celsius
     */
    public boolean isCelsius() {
        return UNIT_CELSIUS.equals(getTemperatureUnit());
    }
    
    // ==================== Default City ====================
    
    /**
     * Set default city
     */
    public void setDefaultCity(String cityName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_DEFAULT_CITY, cityName);
        editor.apply();
    }
    
    /**
     * Get default city (default: Hanoi)
     */
    public String getDefaultCity() {
        return sharedPreferences.getString(KEY_DEFAULT_CITY, "Hanoi");
    }
    
    // ==================== Cached Weather Data ====================
    
    /**
     * Cache weather data for offline viewing
     */
    public void cacheWeatherData(String city, String temperature, String condition) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CACHED_CITY, city);
        editor.putString(KEY_CACHED_TEMP, temperature);
        editor.putString(KEY_CACHED_CONDITION, condition);
        editor.apply();
    }
    
    /**
     * Get cached city
     */
    public String getCachedCity() {
        return sharedPreferences.getString(KEY_CACHED_CITY, "");
    }
    
    /**
     * Get cached temperature
     */
    public String getCachedTemperature() {
        return sharedPreferences.getString(KEY_CACHED_TEMP, "--");
    }
    
    /**
     * Get cached condition
     */
    public String getCachedCondition() {
        return sharedPreferences.getString(KEY_CACHED_CONDITION, "...");
    }
    
    /**
     * Check if cache exists
     */
    public boolean hasCachedData() {
        return !getCachedCity().isEmpty();
    }
    
    // ==================== Last Update Time ====================
    
    /**
     * Set last update time
     */
    public void setLastUpdateTime(long timestamp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_UPDATE_TIME, timestamp);
        editor.apply();
    }
    
    /**
     * Get last update time
     */
    public long getLastUpdateTime() {
        return sharedPreferences.getLong(KEY_LAST_UPDATE_TIME, 0);
    }
    
    /**
     * Clear all cached data
     */
    public void clearCache() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_CACHED_CITY);
        editor.remove(KEY_CACHED_TEMP);
        editor.remove(KEY_CACHED_CONDITION);
        editor.remove(KEY_LAST_UPDATE_TIME);
        editor.apply();
    }
    
    /**
     * Clear all preferences (for testing)
     */
    public void clearAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
