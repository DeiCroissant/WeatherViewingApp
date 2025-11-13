package com.example.weatherviewingapp;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Settings Activity (Ch. 5 - Navigation)
 * Features:
 * - Temperature unit selection (SharedPreferences - Ch. 7.a)
 */
public class SettingsActivity extends AppCompatActivity {
    
    private RadioGroup rgTemperatureUnit;
    private RadioButton rbCelsius, rbFahrenheit;
    
    private SettingsManager settingsManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Enable Up navigation (back to MainActivity)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cài đặt");
        }
        
        // Initialize helpers
        settingsManager = new SettingsManager(this);
        
        // Initialize views
        initViews();
        
        // Load current settings
        loadSettings();
        
        // Set listeners
        setupListeners();
    }
    
    /**
     * Initialize all views
     */
    private void initViews() {
        rgTemperatureUnit = findViewById(R.id.rgTemperatureUnit);
        rbCelsius = findViewById(R.id.rbCelsius);
        rbFahrenheit = findViewById(R.id.rbFahrenheit);
    }
    
    /**
     * Load current settings from SharedPreferences
     */
    private void loadSettings() {
        if (settingsManager.isCelsius()) {
            rbCelsius.setChecked(true);
        } else {
            rbFahrenheit.setChecked(true);
        }
    }
    
    /**
     * Setup all listeners
     */
    private void setupListeners() {
        // RadioGroup listener - save temperature unit preference
        rgTemperatureUnit.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCelsius) {
                settingsManager.setTemperatureUnit(SettingsManager.UNIT_CELSIUS);
            } else if (checkedId == R.id.rbFahrenheit) {
                settingsManager.setTemperatureUnit(SettingsManager.UNIT_FAHRENHEIT);
            }
        });
    }
    
    // ==================== Lifecycle ======================================
    
    @Override
    public boolean onSupportNavigateUp() {
        // Handle Up navigation
        finish();
        return true;
    }
}
