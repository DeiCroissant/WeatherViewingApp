package com.example.weatherviewingapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Database Helper for managing favorite cities (Ch. 7.c)
 * Uses SQLite for local data persistence
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    // Database Info
    private static final String DATABASE_NAME = "WeatherApp.db";
    private static final int DATABASE_VERSION = 2; // Incremented for locations table
    
    // Table Name
    private static final String TABLE_CITIES = "favorite_cities"; // Legacy table
    private static final String TABLE_LOCATIONS = "locations"; // New locations table
    
    // Legacy Column Names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CITY_NAME = "city_name";
    
    // Locations Table Column Names
    private static final String COL_LOC_ID = "id";
    private static final String COL_LOC_CITY_NAME = "city_name";
    private static final String COL_LOC_COUNTRY_CODE = "country_code";
    private static final String COL_LOC_LATITUDE = "latitude";
    private static final String COL_LOC_LONGITUDE = "longitude";
    private static final String COL_LOC_TAG = "tag";
    private static final String COL_LOC_IS_DEFAULT = "is_default";
    private static final String COL_LOC_SORT_ORDER = "sort_order";
    private static final String COL_LOC_LAST_UPDATED = "last_updated";
    
    // SQL to create legacy table
    private static final String SQL_CREATE_TABLE = 
            "CREATE TABLE " + TABLE_CITIES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CITY_NAME + " TEXT NOT NULL UNIQUE" +
            ")";
    
    // SQL to create locations table
    private static final String SQL_CREATE_LOCATIONS_TABLE = 
            "CREATE TABLE " + TABLE_LOCATIONS + " (" +
            COL_LOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_LOC_CITY_NAME + " TEXT NOT NULL, " +
            COL_LOC_COUNTRY_CODE + " TEXT, " +
            COL_LOC_LATITUDE + " REAL NOT NULL, " +
            COL_LOC_LONGITUDE + " REAL NOT NULL, " +
            COL_LOC_TAG + " TEXT, " +
            COL_LOC_IS_DEFAULT + " INTEGER DEFAULT 0, " +
            COL_LOC_SORT_ORDER + " INTEGER DEFAULT 0, " +
            COL_LOC_LAST_UPDATED + " INTEGER DEFAULT 0" +
            ")";
    
    // SQL to drop tables
    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_CITIES;
    private static final String SQL_DROP_LOCATIONS_TABLE = "DROP TABLE IF EXISTS " + TABLE_LOCATIONS;
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the legacy table when database is created
        db.execSQL(SQL_CREATE_TABLE);
        // Create the new locations table
        db.execSQL(SQL_CREATE_LOCATIONS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add locations table in version 2
            db.execSQL(SQL_CREATE_LOCATIONS_TABLE);
        }
    }
    
    // ==================== CRUD Operations ====================
    
    /**
     * CREATE: Add a new city to favorites
     * @return true if added successfully, false if city already exists
     */
    public boolean addCity(String cityName) {
        // Check if city already exists
        if (cityExists(cityName)) {
            return false;
        }
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CITY_NAME, cityName);
        
        long result = db.insert(TABLE_CITIES, null, values);
        db.close();
        
        return result != -1; // -1 means insert failed
    }
    
    /**
     * READ: Get all favorite cities
     * @return List of city names
     */
    public List<String> getAllCities() {
        List<String> cityList = new ArrayList<>();
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_CITIES,
                new String[]{COLUMN_CITY_NAME},
                null, null, null, null,
                COLUMN_CITY_NAME + " ASC" // Sort alphabetically
        );
        
        if (cursor.moveToFirst()) {
            do {
                String cityName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY_NAME));
                cityList.add(cityName);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return cityList;
    }
    
    /**
     * READ: Get city count
     */
    public int getCityCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CITIES, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        db.close();
        
        return count;
    }
    
    /**
     * READ: Check if a city already exists
     */
    public boolean cityExists(String cityName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_CITIES,
                new String[]{COLUMN_ID},
                COLUMN_CITY_NAME + " = ?",
                new String[]{cityName},
                null, null, null
        );
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        
        return exists;
    }
    
    /**
     * DELETE: Remove a city from favorites
     * @return true if deleted successfully
     */
    public boolean deleteCity(String cityName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(
                TABLE_CITIES,
                COLUMN_CITY_NAME + " = ?",
                new String[]{cityName}
        );
        db.close();
        
        return rowsDeleted > 0;
    }
    
    /**
     * DELETE: Clear all cities (for testing)
     */
    public void deleteAllCities() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CITIES, null, null);
        db.close();
    }
    
    /**
     * UPDATE: Rename a city (optional feature)
     */
    public boolean updateCity(String oldName, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CITY_NAME, newName);
        
        int rowsUpdated = db.update(
                TABLE_CITIES,
                values,
                COLUMN_CITY_NAME + " = ?",
                new String[]{oldName}
        );
        db.close();
        
        return rowsUpdated > 0;
    }
    
    // ==================== LOCATION CRUD Operations ====================
    
    /**
     * CREATE: Add a new location
     */
    public long addLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COL_LOC_CITY_NAME, location.getCityName());
        values.put(COL_LOC_COUNTRY_CODE, location.getCountryCode());
        values.put(COL_LOC_LATITUDE, location.getLatitude());
        values.put(COL_LOC_LONGITUDE, location.getLongitude());
        values.put(COL_LOC_TAG, location.getTag());
        values.put(COL_LOC_IS_DEFAULT, location.isDefault() ? 1 : 0);
        values.put(COL_LOC_SORT_ORDER, location.getSortOrder());
        values.put(COL_LOC_LAST_UPDATED, location.getLastUpdated());
        
        long id = db.insert(TABLE_LOCATIONS, null, values);
        db.close();
        
        return id;
    }
    
    /**
     * READ: Get all locations ordered by sort_order
     */
    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<>();
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_LOCATIONS,
                null, // Select all columns
                null, null, null, null,
                COL_LOC_SORT_ORDER + " ASC, " + COL_LOC_ID + " ASC"
        );
        
        if (cursor.moveToFirst()) {
            do {
                Location location = cursorToLocation(cursor);
                locations.add(location);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return locations;
    }
    
    /**
     * READ: Get location by ID
     */
    public Location getLocationById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_LOCATIONS,
                null,
                COL_LOC_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );
        
        Location location = null;
        if (cursor.moveToFirst()) {
            location = cursorToLocation(cursor);
        }
        
        cursor.close();
        db.close();
        
        return location;
    }
    
    /**
     * READ: Get default location
     */
    public Location getDefaultLocation() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_LOCATIONS,
                null,
                COL_LOC_IS_DEFAULT + " = 1",
                null, null, null, null,
                "1" // Limit 1
        );
        
        Location location = null;
        if (cursor.moveToFirst()) {
            location = cursorToLocation(cursor);
        }
        
        cursor.close();
        db.close();
        
        return location;
    }
    
    /**
     * UPDATE: Update a location
     */
    public boolean updateLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COL_LOC_CITY_NAME, location.getCityName());
        values.put(COL_LOC_COUNTRY_CODE, location.getCountryCode());
        values.put(COL_LOC_LATITUDE, location.getLatitude());
        values.put(COL_LOC_LONGITUDE, location.getLongitude());
        values.put(COL_LOC_TAG, location.getTag());
        values.put(COL_LOC_IS_DEFAULT, location.isDefault() ? 1 : 0);
        values.put(COL_LOC_SORT_ORDER, location.getSortOrder());
        values.put(COL_LOC_LAST_UPDATED, location.getLastUpdated());
        
        int rowsUpdated = db.update(
                TABLE_LOCATIONS,
                values,
                COL_LOC_ID + " = ?",
                new String[]{String.valueOf(location.getId())}
        );
        db.close();
        
        return rowsUpdated > 0;
    }
    
    /**
     * UPDATE: Set a location as default (clears other defaults)
     */
    public boolean setDefaultLocation(int locationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Clear all defaults first
        ContentValues clearValues = new ContentValues();
        clearValues.put(COL_LOC_IS_DEFAULT, 0);
        db.update(TABLE_LOCATIONS, clearValues, null, null);
        
        // Set new default
        ContentValues values = new ContentValues();
        values.put(COL_LOC_IS_DEFAULT, 1);
        int rowsUpdated = db.update(
                TABLE_LOCATIONS,
                values,
                COL_LOC_ID + " = ?",
                new String[]{String.valueOf(locationId)}
        );
        db.close();
        
        return rowsUpdated > 0;
    }
    
    /**
     * DELETE: Remove a location
     */
    public boolean deleteLocation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(
                TABLE_LOCATIONS,
                COL_LOC_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
        db.close();
        
        return rowsDeleted > 0;
    }
    
    /**
     * DELETE: Clear all locations
     */
    public void deleteAllLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATIONS, null, null);
        db.close();
    }
    
    /**
     * Helper: Convert cursor to Location object
     */
    private Location cursorToLocation(Cursor cursor) {
        Location location = new Location();
        
        location.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOC_ID)));
        location.setCityName(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOC_CITY_NAME)));
        location.setCountryCode(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOC_COUNTRY_CODE)));
        location.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LOC_LATITUDE)));
        location.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LOC_LONGITUDE)));
        location.setTag(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOC_TAG)));
        location.setDefault(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOC_IS_DEFAULT)) == 1);
        location.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOC_SORT_ORDER)));
        location.setLastUpdated(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOC_LAST_UPDATED)));
        
        return location;
    }
    
    /**
     * Get location count
     */
    public int getLocationCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LOCATIONS, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        
        cursor.close();
        db.close();
        
        return count;
    }
}
