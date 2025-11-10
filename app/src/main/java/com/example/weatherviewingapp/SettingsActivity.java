package com.example.weatherviewingapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Settings Activity (Ch. 5 - Navigation)
 * Features:
 * - Temperature unit selection (SharedPreferences - Ch. 7.a)
 * - City management (SQLite - Ch. 7.c)
 * - Context Menu for deletion (Ch. 4)
 * - AlertDialog for confirmation (Ch. 4)
 */
public class SettingsActivity extends AppCompatActivity {
    
    private RadioGroup rgTemperatureUnit;
    private RadioButton rbCelsius, rbFahrenheit;
    private EditText etCityName;
    private Button btnAddCity;
    private ListView lvCities;
    private TextView tvNoCities;
    
    private SettingsManager settingsManager;
    private DatabaseHelper databaseHelper;
    private ArrayAdapter<String> cityAdapter;
    private List<String> cityList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // Enable Up navigation (back to MainActivity)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Initialize helpers
        settingsManager = new SettingsManager(this);
        databaseHelper = new DatabaseHelper(this);
        
        // Initialize views
        initViews();
        
        // Load current settings
        loadSettings();
        
        // Load cities from database
        loadCities();
        
        // Set listeners
        setupListeners();
        
        // Register context menu for ListView
        registerForContextMenu(lvCities);
    }
    
    /**
     * Initialize all views
     */
    private void initViews() {
        rgTemperatureUnit = findViewById(R.id.rgTemperatureUnit);
        rbCelsius = findViewById(R.id.rbCelsius);
        rbFahrenheit = findViewById(R.id.rbFahrenheit);
        etCityName = findViewById(R.id.etCityName);
        btnAddCity = findViewById(R.id.btnAddCity);
        lvCities = findViewById(R.id.lvCities);
        tvNoCities = findViewById(R.id.tvNoCities);
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
     * Load cities from database and display in ListView
     */
    private void loadCities() {
        cityList = databaseHelper.getAllCities();
        
        if (cityList.isEmpty()) {
            tvNoCities.setVisibility(View.VISIBLE);
            lvCities.setVisibility(View.GONE);
        } else {
            tvNoCities.setVisibility(View.GONE);
            lvCities.setVisibility(View.VISIBLE);
            
            // Create adapter
            cityAdapter = new ArrayAdapter<>(
                this,
                R.layout.list_item_city,
                R.id.tvCityItemName,
                cityList
            );
            lvCities.setAdapter(cityAdapter);
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
        
        // Add City button listener
        btnAddCity.setOnClickListener(v -> addCity());
        
        // ListView item click - set as default city
        lvCities.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = cityList.get(position);
            settingsManager.setDefaultCity(selectedCity);
            Toast.makeText(this, "Đã chọn " + selectedCity + " làm thành phố mặc định", 
                          Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Add a new city to database
     */
    private void addCity() {
        String cityName = etCityName.getText().toString().trim();
        
        if (cityName.isEmpty()) {
            Toast.makeText(this, R.string.city_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean added = databaseHelper.addCity(cityName);
        
        if (added) {
            Toast.makeText(this, R.string.city_added, Toast.LENGTH_SHORT).show();
            etCityName.setText(""); // Clear input
            loadCities(); // Reload list
        } else {
            Toast.makeText(this, R.string.city_exists, Toast.LENGTH_SHORT).show();
        }
    }
    
    // ==================== Context Menu (Ch. 4) ====================
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, 
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        if (v.getId() == R.id.lvCities) {
            getMenuInflater().inflate(R.menu.menu_context_city, menu);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            // Get selected city
            AdapterView.AdapterContextMenuInfo info = 
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            String selectedCity = cityList.get(info.position);
            
            // Show confirmation dialog
            showDeleteConfirmDialog(selectedCity);
            return true;
        }
        return super.onContextItemSelected(item);
    }
    
    /**
     * Show AlertDialog to confirm deletion (Ch. 4)
     */
    private void showDeleteConfirmDialog(String cityName) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(getString(R.string.confirm_delete_message, cityName))
            .setPositiveButton(R.string.btn_ok, (dialog, which) -> {
                // Delete city
                boolean deleted = databaseHelper.deleteCity(cityName);
                if (deleted) {
                    Toast.makeText(this, R.string.city_deleted, Toast.LENGTH_SHORT).show();
                    loadCities(); // Reload list
                }
            })
            .setNegativeButton(R.string.btn_cancel, null)
            .show();
    }
    
    // ==================== Lifecycle ====================
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection
        databaseHelper.close();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        // Handle Up navigation
        finish();
        return true;
    }
}
