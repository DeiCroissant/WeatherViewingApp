package com.example.weatherviewingapp;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Manage Locations Activity
 * Allows users to search, add, remove, and manage saved locations
 */
public class ManageLocationsActivity extends AppCompatActivity {
    
    private AutoCompleteTextView etSearchCity;
    private ListView lvLocations;
    private ProgressBar progressBar;
    private TextView tvNoLocations;
    
    private DatabaseHelper dbHelper;
    private LocationAdapter locationAdapter;
    private List<Location> locations;
    private List<String> citySearchResults;
    
    private static final String API_KEY = "f4f8b8c6cb6bc92ebc999dd51c61bf4e";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_locations);
        
        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Locations");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initViews();
        loadLocations();
        setupSearchAutocomplete();
    }
    
    private void initViews() {
        etSearchCity = findViewById(R.id.etSearchCity);
        lvLocations = findViewById(R.id.lvLocations);
        progressBar = findViewById(R.id.progressBar);
        tvNoLocations = findViewById(R.id.tvNoLocations);
        
        dbHelper = new DatabaseHelper(this);
        locations = new ArrayList<>();
        citySearchResults = new ArrayList<>();
    }
    
    private void loadLocations() {
        locations = dbHelper.getAllLocations();
        
        if (locations.isEmpty()) {
            tvNoLocations.setVisibility(View.VISIBLE);
            lvLocations.setVisibility(View.GONE);
        } else {
            tvNoLocations.setVisibility(View.GONE);
            lvLocations.setVisibility(View.VISIBLE);
            
            locationAdapter = new LocationAdapter(locations);
            lvLocations.setAdapter(locationAdapter);
        }
    }
    
    private void setupSearchAutocomplete() {
        etSearchCity.setThreshold(2); // Start autocomplete after 2 characters
        
        etSearchCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) {
                    searchCities(s.toString());
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        etSearchCity.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = (String) parent.getItemAtPosition(position);
            showAddLocationDialog(selectedCity);
        });
    }
    
    private void searchCities(String query) {
        new SearchCitiesTask().execute(query);
    }
    
    private void showAddLocationDialog(String cityName) {
        // Parse city name (format: "City, Country")
        String[] parts = cityName.split(", ");
        if (parts.length < 2) {
            Toast.makeText(this, "Invalid city format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String city = parts[0];
        String country = parts[1];
        
        // Fetch coordinates for this city
        new FetchCityCoordinatesTask(city, country).execute();
    }
    
    private void addLocation(Location location) {
        long id = dbHelper.addLocation(location);
        
        if (id > 0) {
            location.setId((int) id);
            Toast.makeText(this, "Added " + location.getCityName(), Toast.LENGTH_SHORT).show();
            etSearchCity.setText("");
            loadLocations();
        } else {
            Toast.makeText(this, "Failed to add location", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void deleteLocation(Location location) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Location")
            .setMessage("Remove " + location.getDisplayName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> {
                if (dbHelper.deleteLocation(location.getId())) {
                    Toast.makeText(this, "Deleted " + location.getCityName(), Toast.LENGTH_SHORT).show();
                    loadLocations();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showEditTagDialog(Location location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Tag for " + location.getCityName());
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_tag, null);
        EditText etTag = dialogView.findViewById(R.id.etTag);
        etTag.setText(location.getTag());
        
        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String tag = etTag.getText().toString().trim();
            location.setTag(tag);
            if (dbHelper.updateLocation(location)) {
                Toast.makeText(this, "Tag updated", Toast.LENGTH_SHORT).show();
                loadLocations();
            }
        });
        builder.setNegativeButton("Cancel", null);
        
        // Add quick tag buttons
        Button btnHome = dialogView.findViewById(R.id.btnTagHome);
        Button btnWork = dialogView.findViewById(R.id.btnTagWork);
        Button btnClear = dialogView.findViewById(R.id.btnTagClear);
        
        btnHome.setOnClickListener(v -> etTag.setText("Home"));
        btnWork.setOnClickListener(v -> etTag.setText("Work"));
        btnClear.setOnClickListener(v -> etTag.setText(""));
        
        builder.show();
    }
    
    private void setDefaultLocation(Location location) {
        if (dbHelper.setDefaultLocation(location.getId())) {
            Toast.makeText(this, location.getCityName() + " set as default", Toast.LENGTH_SHORT).show();
            loadLocations();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    // ==================== AsyncTasks ====================
    
    /**
     * Search cities using OpenWeather Geocoding API
     */
    private class SearchCitiesTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        @Override
        protected List<String> doInBackground(String... params) {
            String query = params[0];
            List<String> results = new ArrayList<>();
            
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String urlString = "http://api.openweathermap.org/geo/1.0/direct?q=" + 
                                   encodedQuery + "&limit=5&appid=" + API_KEY;
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                reader.close();
                connection.disconnect();
                
                // Parse JSON response
                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject city = jsonArray.getJSONObject(i);
                    String name = city.getString("name");
                    String country = city.optString("country", "");
                    String state = city.optString("state", "");
                    
                    String displayName = name;
                    if (!state.isEmpty()) {
                        displayName += ", " + state;
                    }
                    if (!country.isEmpty()) {
                        displayName += ", " + country;
                    }
                    
                    results.add(displayName);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return results;
        }
        
        @Override
        protected void onPostExecute(List<String> results) {
            progressBar.setVisibility(View.GONE);
            citySearchResults = results;
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                ManageLocationsActivity.this,
                android.R.layout.simple_dropdown_item_1line,
                results
            );
            etSearchCity.setAdapter(adapter);
            
            if (!results.isEmpty()) {
                etSearchCity.showDropDown();
            }
        }
    }
    
    /**
     * Fetch coordinates for a selected city
     */
    private class FetchCityCoordinatesTask extends AsyncTask<Void, Void, Location> {
        private String cityName;
        private String countryCode;
        
        public FetchCityCoordinatesTask(String cityName, String countryCode) {
            this.cityName = cityName;
            this.countryCode = countryCode;
        }
        
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        @Override
        protected Location doInBackground(Void... params) {
            try {
                String query = cityName + "," + countryCode;
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String urlString = "http://api.openweathermap.org/geo/1.0/direct?q=" + 
                                   encodedQuery + "&limit=1&appid=" + API_KEY;
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                reader.close();
                connection.disconnect();
                
                // Parse JSON response
                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject city = jsonArray.getJSONObject(0);
                    
                    String name = city.getString("name");
                    String country = city.optString("country", "");
                    double lat = city.getDouble("lat");
                    double lon = city.getDouble("lon");
                    
                    return new Location(name, country, lat, lon);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Location location) {
            progressBar.setVisibility(View.GONE);
            
            if (location != null) {
                addLocation(location);
            } else {
                Toast.makeText(ManageLocationsActivity.this, 
                    "Failed to fetch coordinates", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    // ==================== Location Adapter ====================
    
    private class LocationAdapter extends ArrayAdapter<Location> {
        
        public LocationAdapter(List<Location> locations) {
            super(ManageLocationsActivity.this, 0, locations);
        }
        
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_location, parent, false);
            }
            
            Location location = getItem(position);
            if (location == null) return convertView;
            
            TextView tvCityName = convertView.findViewById(R.id.tvCityName);
            TextView tvCoordinates = convertView.findViewById(R.id.tvCoordinates);
            TextView tvTag = convertView.findViewById(R.id.tvTag);
            ImageButton btnSetDefault = convertView.findViewById(R.id.btnSetDefault);
            ImageButton btnEditTag = convertView.findViewById(R.id.btnEditTag);
            ImageButton btnDelete = convertView.findViewById(R.id.btnDelete);
            
            // Display location info
            tvCityName.setText(location.getFullName());
            tvCoordinates.setText(String.format("%.2f, %.2f", 
                location.getLatitude(), location.getLongitude()));
            
            // Show tag if exists
            if (location.getTag() != null && !location.getTag().isEmpty()) {
                tvTag.setVisibility(View.VISIBLE);
                tvTag.setText(location.getTag());
            } else {
                tvTag.setVisibility(View.GONE);
            }
            
            // Highlight default location
            if (location.isDefault()) {
                convertView.setBackgroundColor(0x1A4CAF50); // Light green tint
                btnSetDefault.setAlpha(0.5f);
            } else {
                convertView.setBackgroundColor(0x00000000); // Transparent
                btnSetDefault.setAlpha(1.0f);
            }
            
            // Button listeners
            btnSetDefault.setOnClickListener(v -> setDefaultLocation(location));
            btnEditTag.setOnClickListener(v -> showEditTagDialog(location));
            btnDelete.setOnClickListener(v -> deleteLocation(location));
            
            return convertView;
        }
    }
}
