package com.example.weatherviewingapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Debug Activity để xem nội dung Database
 * CHỈ DÙNG ĐỂ DEBUG - XÓA TRƯỚC KHI RELEASE
 */
public class DebugDatabaseActivity extends AppCompatActivity {
    
    private TextView tvDebugInfo;
    private ListView lvDebugCities;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_database);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Debug Database");
        }
        
        tvDebugInfo = findViewById(R.id.tvDebugInfo);
        lvDebugCities = findViewById(R.id.lvDebugCities);
        
        loadDatabaseInfo();
    }
    
    private void loadDatabaseInfo() {
        DatabaseHelper db = new DatabaseHelper(this);
        
        // Get all locations from the NEW table
        List<Location> locations = db.getAllLocations();
        int count = locations.size();
        
        // Display info
        StringBuilder info = new StringBuilder();
        info.append("📊 DATABASE INFO\n");
        info.append("================\n\n");
        info.append("Database: WeatherApp.db\n");
        info.append("Table: locations\n");
        info.append("Total Locations: ").append(count).append("\n\n");
        info.append("Columns:\n");
        info.append("  - id (INTEGER PRIMARY KEY)\n");
        info.append("  - city_name (TEXT)\n");
        info.append("  - country_code (TEXT)\n");
        info.append("  - latitude (REAL)\n");
        info.append("  - longitude (REAL)\n");
        info.append("  - tag (TEXT)\n");
        info.append("  - is_default (INTEGER)\n");
        info.append("  - sort_order (INTEGER)\n");
        info.append("  - last_updated (INTEGER)\n\n");
        info.append("Locations List:\n");
        info.append("================\n");
        
        if (locations.isEmpty()) {
            info.append("(No locations in database)\n");
        } else {
            for (int i = 0; i < locations.size(); i++) {
                Location loc = locations.get(i);
                info.append(String.format("%d. %s", i + 1, loc.getCityName()));
                if (loc.getCountryCode() != null && !loc.getCountryCode().isEmpty()) {
                    info.append(", ").append(loc.getCountryCode());
                }
                if (loc.isDefault()) {
                    info.append(" [DEFAULT]");
                }
                if (loc.getTag() != null && !loc.getTag().isEmpty()) {
                    info.append(" (").append(loc.getTag()).append(")");
                }
                info.append("\n");
                info.append(String.format("   Coords: %.4f, %.4f\n", 
                    loc.getLatitude(), loc.getLongitude()));
            }
        }
        
        tvDebugInfo.setText(info.toString());
        
        // Show in ListView - convert to display strings
        List<String> displayStrings = new ArrayList<>();
        for (Location loc : locations) {
            String display = loc.getDisplayName();
            if (loc.isDefault()) {
                display += " ⭐";
            }
            displayStrings.add(display);
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            displayStrings
        );
        lvDebugCities.setAdapter(adapter);
        
        db.close();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
