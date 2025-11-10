package com.example.weatherviewingapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
        
        // Get all cities
        List<String> cities = db.getAllCities();
        int count = db.getCityCount();
        
        // Display info
        StringBuilder info = new StringBuilder();
        info.append("📊 DATABASE INFO\n");
        info.append("================\n\n");
        info.append("Database: WeatherApp.db\n");
        info.append("Table: favorite_cities\n");
        info.append("Total Cities: ").append(count).append("\n\n");
        info.append("Columns:\n");
        info.append("  - id (INTEGER PRIMARY KEY)\n");
        info.append("  - city_name (TEXT UNIQUE)\n\n");
        info.append("Cities List:\n");
        info.append("================\n");
        
        if (cities.isEmpty()) {
            info.append("(No cities in database)\n");
        } else {
            for (int i = 0; i < cities.size(); i++) {
                info.append(String.format("%d. %s\n", i + 1, cities.get(i)));
            }
        }
        
        tvDebugInfo.setText(info.toString());
        
        // Show in ListView too
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            cities
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
