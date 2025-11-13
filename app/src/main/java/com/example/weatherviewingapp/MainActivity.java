package com.example.weatherviewingapp;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main Activity - Enhanced Weather Display Screen
 * Features:
 * - Beautiful weather icons and gradients
 * - Pull-to-refresh functionality
 * - Skeleton loading animation
 * - Detailed error states
 * - Last update timestamp
 * - Fade-in animations
 * - Network connectivity check
 */
public class MainActivity extends AppCompatActivity {
    
    private AutoCompleteTextView etSearchCity;
    private ImageView ivMenuIcon;
    private TextView tvCityName;
    private ImageView ivWeatherIcon;
    private TextView tvTemperature;
    private TextView tvCondition;
    private TextView tvLastUpdated;
    private Button btnRefresh;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout mainLayout;
    
    // Weather details views
    private TextView tvFeelsLike, tvHumidity, tvWind, tvPressure;
    private TextView tvVisibility, tvSunrise, tvSunset, tvUV, tvClouds;
    
    // Forecast views
    private LinearLayout forecastContainer;
    
    // Skeleton views
    private View skeletonCity, skeletonIcon, skeletonTemp, skeletonCondition;
    
    // Error state views
    private LinearLayout errorContainer;
    private ImageView ivErrorIcon;
    private TextView tvErrorMessage;
    private Button btnRetry;
    
    private SettingsManager settingsManager;
    private WeatherApiClient weatherApiClient;
    
    private String currentCity;
    private double currentLat = 0;
    private double currentLon = 0;
    private boolean isLoading = false;
    
    private static final String API_KEY = "c087fa97752f540e360b43023b2d945a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize helpers
        settingsManager = new SettingsManager(this);
        weatherApiClient = new WeatherApiClient();
        
        // Initialize views
        initViews();
        
        // Load cached data first (for offline viewing)
        loadCachedData();
        
        // Get default city from SharedPreferences
        currentCity = settingsManager.getDefaultCity();
        
        // Fetch fresh weather data
        fetchWeatherData();
        
        // Setup listeners
        setupListeners();
    }
    
    /**
     * Initialize all views
     */
    private void initViews() {
        mainLayout = findViewById(R.id.main);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        ivMenuIcon = findViewById(R.id.ivMenuIcon);
        etSearchCity = findViewById(R.id.etSearchCity);
        tvCityName = findViewById(R.id.tvCityName);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        btnRefresh = findViewById(R.id.btnRefresh);
        
        // Weather details views
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvPressure = findViewById(R.id.tvPressure);
        tvVisibility = findViewById(R.id.tvVisibility);
        tvSunrise = findViewById(R.id.tvSunrise);
        tvSunset = findViewById(R.id.tvSunset);
        tvUV = findViewById(R.id.tvUV);
        tvClouds = findViewById(R.id.tvClouds);
        
        // Forecast container
        forecastContainer = findViewById(R.id.forecastContainer);
        
        // Skeleton views
        skeletonCity = findViewById(R.id.skeletonCity);
        skeletonIcon = findViewById(R.id.skeletonIcon);
        skeletonTemp = findViewById(R.id.skeletonTemp);
        skeletonCondition = findViewById(R.id.skeletonCondition);
        
        // Error views
        errorContainer = findViewById(R.id.errorContainer);
        ivErrorIcon = findViewById(R.id.ivErrorIcon);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRetry = findViewById(R.id.btnRetry);
        
        // Configure SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeColors(
            getResources().getColor(android.R.color.holo_blue_bright),
            getResources().getColor(android.R.color.holo_green_light),
            getResources().getColor(android.R.color.holo_orange_light)
        );
    }
    
    /**
     * Setup listeners
     */
    private void setupListeners() {
        // Menu icon click listener
        ivMenuIcon.setOnClickListener(v -> {
            showPopupMenu(v);
        });
        
        // Search city autocomplete
        etSearchCity.setThreshold(2);
        etSearchCity.setDropDownBackgroundResource(android.R.color.transparent);
        
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
            etSearchCity.setText(""); // Clear search box
            etSearchCity.dismissDropDown(); // Dismiss dropdown
            fetchWeatherForCity(selectedCity);
            hideKeyboard();
        });
        
        etSearchCity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String city = etSearchCity.getText().toString().trim();
                if (!city.isEmpty()) {
                    fetchWeatherForCity(city);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });
        
        // Refresh button listener
        btnRefresh.setOnClickListener(v -> {
            if (!isLoading) {
                fetchWeatherData();
            }
        });
        
        // SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchWeatherData();
        });
        
        // Retry button listener
        btnRetry.setOnClickListener(v -> {
            fetchWeatherData();
        });
    }
    
    
    /**
     * Load cached weather data (for offline viewing)
     */
    private void loadCachedData() {
        if (settingsManager.hasCachedData()) {
            String cachedCity = settingsManager.getCachedCity();
            String cachedTemp = settingsManager.getCachedTemperature();
            String cachedCondition = settingsManager.getCachedCondition();
            
            tvCityName.setText(cachedCity);
            tvTemperature.setText(cachedTemp);
            tvCondition.setText(cachedCondition);
            
            // Show last update time if available
            long lastUpdate = settingsManager.getLastUpdateTime();
            if (lastUpdate > 0) {
                updateLastUpdatedTime(lastUpdate);
            }
        }
    }
    
    /**
     * Check if device has internet connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    /**
     * Show skeleton loading animation
     */
    private void showSkeletonLoading() {
        isLoading = true;
        btnRefresh.setEnabled(false);
        
        // Hide content
        tvCityName.setVisibility(View.GONE);
        ivWeatherIcon.setVisibility(View.GONE);
        tvTemperature.setVisibility(View.GONE);
        tvCondition.setVisibility(View.GONE);
        tvLastUpdated.setVisibility(View.GONE);
        errorContainer.setVisibility(View.GONE);
        
        // Show skeleton
        skeletonCity.setVisibility(View.VISIBLE);
        skeletonIcon.setVisibility(View.VISIBLE);
        skeletonTemp.setVisibility(View.VISIBLE);
        skeletonCondition.setVisibility(View.VISIBLE);
        
        // Start shimmer animation
        Animation shimmer = AnimationUtils.loadAnimation(this, R.anim.skeleton_shimmer);
        skeletonCity.startAnimation(shimmer);
        skeletonIcon.startAnimation(shimmer);
        skeletonTemp.startAnimation(shimmer);
        skeletonCondition.startAnimation(shimmer);
    }
    
    /**
     * Hide skeleton loading animation
     */
    private void hideSkeletonLoading() {
        isLoading = false;
        btnRefresh.setEnabled(true);
        swipeRefreshLayout.setRefreshing(false);
        
        // Hide skeleton
        skeletonCity.clearAnimation();
        skeletonIcon.clearAnimation();
        skeletonTemp.clearAnimation();
        skeletonCondition.clearAnimation();
        
        skeletonCity.setVisibility(View.GONE);
        skeletonIcon.setVisibility(View.GONE);
        skeletonTemp.setVisibility(View.GONE);
        skeletonCondition.setVisibility(View.GONE);
    }
    
    /**
     * Show error state
     */
    private void showErrorState(String errorMessage, ErrorType errorType) {
        hideSkeletonLoading();
        
        // Hide content
        tvCityName.setVisibility(View.GONE);
        ivWeatherIcon.setVisibility(View.GONE);
        tvTemperature.setVisibility(View.GONE);
        tvCondition.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);
        
        // Show error
        errorContainer.setVisibility(View.VISIBLE);
        tvErrorMessage.setText(errorMessage);
        
        // Set error icon based on type
        switch (errorType) {
            case NO_INTERNET:
                ivErrorIcon.setImageResource(android.R.drawable.stat_notify_error);
                break;
            case API_ERROR:
                ivErrorIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                break;
            case GPS_DISABLED:
                ivErrorIcon.setImageResource(android.R.drawable.ic_menu_mylocation);
                break;
            default:
                ivErrorIcon.setImageResource(android.R.drawable.stat_notify_error);
        }
        
        // If we have cached data and it's a network error, show it
        if (errorType == ErrorType.NO_INTERNET && settingsManager.hasCachedData()) {
            Toast.makeText(this, R.string.using_cached_data, Toast.LENGTH_LONG).show();
            showContent();
            loadCachedData();
        }
    }
    
    /**
     * Show content (hide error state)
     */
    private void showContent() {
        hideSkeletonLoading();
        
        errorContainer.setVisibility(View.GONE);
        tvCityName.setVisibility(View.VISIBLE);
        ivWeatherIcon.setVisibility(View.VISIBLE);
        tvTemperature.setVisibility(View.VISIBLE);
        tvCondition.setVisibility(View.VISIBLE);
        tvLastUpdated.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.VISIBLE);
        
        // Apply fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvCityName.startAnimation(fadeIn);
        ivWeatherIcon.startAnimation(fadeIn);
        tvTemperature.startAnimation(fadeIn);
        tvCondition.startAnimation(fadeIn);
    }
    
    /**
     * Update last updated time
     */
    private void updateLastUpdatedTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeString = sdf.format(new Date(timestamp));
        tvLastUpdated.setText(getString(R.string.last_updated, timeString));
        tvLastUpdated.setVisibility(View.VISIBLE);
    }
    
    /**
     * Check if it's night time (for background gradient)
     */
    private boolean isNightTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour < 6 || hour >= 18; // Night from 6 PM to 6 AM
    }
    
    enum ErrorType {
        NO_INTERNET,
        API_ERROR,
        GPS_DISABLED,
        API_QUOTA
    }
    
    
    /**
     * Fetch weather data from API
     */
    private void fetchWeatherData() {
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            showErrorState(getString(R.string.error_no_internet), ErrorType.NO_INTERNET);
            return;
        }
        
        // Use currentCity from settings (don't reset!)
        // currentCity is already set in onCreate() and onResume()
        currentLat = 0;
        currentLon = 0;
        
        // Show skeleton loading
        showSkeletonLoading();
        
        // Call API
        weatherApiClient.fetchWeather(currentCity, new WeatherApiClient.WeatherCallback() {
            @Override
            public void onSuccess(WeatherApiClient.WeatherData weatherData) {
                showContent();
                displayWeatherData(weatherData);
                
                // Update last update time
                long currentTime = System.currentTimeMillis();
                settingsManager.setLastUpdateTime(currentTime);
                updateLastUpdatedTime(currentTime);
            }
            
            @Override
            public void onError(String errorMessage) {
                // Determine error type
                ErrorType errorType = ErrorType.API_ERROR;
                String displayMessage = errorMessage;
                
                if (errorMessage.contains("429") || errorMessage.contains("quota")) {
                    errorType = ErrorType.API_QUOTA;
                    displayMessage = getString(R.string.error_api_quota);
                } else if (errorMessage.contains("network") || errorMessage.contains("timeout")) {
                    errorType = ErrorType.NO_INTERNET;
                    displayMessage = getString(R.string.error_network);
                }
                
                showErrorState(displayMessage, errorType);
            }
        });
    }
    
    /**
     * Display weather data on UI with animations and gradient background
     */
    private void displayWeatherData(WeatherApiClient.WeatherData weatherData) {
        // Show content views (city name, icon, temperature, condition)
        showContent();
        
        // City name - use currentCity if available, otherwise use weatherData.cityName
        if (currentCity != null && !currentCity.isEmpty()) {
            tvCityName.setText(currentCity);
        } else {
            tvCityName.setText(weatherData.cityName);
        }
        
        // Temperature (with unit conversion based on preferences)
        String tempText;
        if (settingsManager.isCelsius()) {
            tempText = String.format(Locale.getDefault(), "%.1f°C", weatherData.temperature);
        } else {
            tempText = String.format(Locale.getDefault(), "%.1f°F", 
                                    weatherData.getTemperatureInFahrenheit());
        }
        tvTemperature.setText(tempText);
        
        // Condition
        tvCondition.setText(weatherData.description);
        
        // Weather icon
        int iconResId = WeatherApiClient.getWeatherIcon(weatherData.weatherId);
        ivWeatherIcon.setImageResource(iconResId);
        
        // Set background gradient based on weather and temperature
        int gradientResId = WeatherApiClient.getBackgroundGradient(
            weatherData.weatherId, 
            weatherData.temperature, 
            isNightTime()
        );
        mainLayout.setBackgroundResource(gradientResId);
        
        // Display extended weather details
        displayWeatherDetails(weatherData);
        
        // Load 5-day forecast
        String cityForForecast = currentCity != null ? currentCity : weatherData.cityName;
        loadForecast(cityForForecast);
        
        // Cache data for offline viewing
        String cityToCache = currentCity != null ? currentCity : weatherData.cityName;
        settingsManager.cacheWeatherData(cityToCache, tempText, weatherData.description);
    }
    
    /**
     * Display extended weather details
     */
    private void displayWeatherDetails(WeatherApiClient.WeatherData data) {
        // Feels Like
        String feelsLikeText;
        if (settingsManager.isCelsius()) {
            feelsLikeText = String.format(Locale.getDefault(), "%.0f°", data.feelsLike);
        } else {
            feelsLikeText = String.format(Locale.getDefault(), "%.0f°", data.getFeelsLikeInFahrenheit());
        }
        tvFeelsLike.setText(feelsLikeText);
        
        // Humidity
        tvHumidity.setText(String.format(Locale.getDefault(), "%d%%", data.humidity));
        
        // Wind
        String windText = String.format(Locale.getDefault(), "%.1f m/s %s", 
                                       data.windSpeed, data.getWindDirection());
        tvWind.setText(windText);
        
        // Pressure
        tvPressure.setText(String.format(Locale.getDefault(), "%d hPa", data.pressure));
        
        // Visibility
        double visibilityKm = data.visibility / 1000.0;
        tvVisibility.setText(String.format(Locale.getDefault(), "%.1f km", visibilityKm));
        
        // Sunrise & Sunset
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (data.sunrise > 0) {
            tvSunrise.setText(timeFormat.format(new Date(data.sunrise * 1000)));
        } else {
            tvSunrise.setText("--:--");
        }
        
        if (data.sunset > 0) {
            tvSunset.setText(timeFormat.format(new Date(data.sunset * 1000)));
        } else {
            tvSunset.setText("--:--");
        }
        
        // UV Index (placeholder - need separate API call)
        if (data.uvIndex > 0) {
            tvUV.setText(String.format(Locale.getDefault(), "%.0f (%s)", 
                                      data.uvIndex, data.getUvIndexLevel()));
        } else {
            tvUV.setText("N/A");
        }
        
        // Clouds
        tvClouds.setText(String.format(Locale.getDefault(), "%d%%", data.clouds));
    }
    
    
    // ==================== Options Menu (Ch. 4) ====================
    
    /**
     * Show popup menu when menu icon is clicked
     */
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());
        
        // Force show icons in popup menu
        try {
            java.lang.reflect.Field fieldPopup = popupMenu.getClass().getDeclaredField("mPopup");
            fieldPopup.setAccessible(true);
            Object menuPopupHelper = fieldPopup.get(popupMenu);
            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
            java.lang.reflect.Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
            setForceIcons.invoke(menuPopupHelper, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        popupMenu.setOnMenuItemClickListener(item -> {
            return onOptionsItemSelected(item);
        });
        
        popupMenu.show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_manage_cities) {
            // Navigate to Manage Locations Activity
            Intent intent = new Intent(MainActivity.this, ManageLocationsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_settings) {
            // Navigate to SettingsActivity
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_debug_database) {
            // Navigate to Debug Database Activity (for development)
            Intent intent = new Intent(MainActivity.this, DebugDatabaseActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            // Navigate to Team Activity
            Intent intent = new Intent(MainActivity.this, TeamActivity.class);
            startActivity(intent);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    // ==================== Lifecycle (Ch. 6) ====================
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if settings changed (user might have changed unit or default city)
        String newDefaultCity = settingsManager.getDefaultCity();
        Log.d("MainActivity", "onResume - currentCity: " + currentCity + ", newDefaultCity: " + newDefaultCity);
        
        if (!currentCity.equals(newDefaultCity)) {
            Log.d("MainActivity", "City changed! Fetching weather for: " + newDefaultCity);
            currentCity = newDefaultCity;
            fetchWeatherData(); // Refresh if city changed
        } else {
            // Just update display if only unit changed
            if (settingsManager.hasCachedData()) {
                // Re-fetch to update temperature unit
                fetchWeatherData();
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        swipeRefreshLayout.setRefreshing(false);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up
    }
    
    // ==================== City Search Methods ====================
    
    /**
     * Search cities using OpenWeather Geocoding API
     */
    private void searchCities(String query) {
        new SearchCitiesTask().execute(query);
    }
    
    /**
     * Fetch weather for selected city
     */
    private void fetchWeatherForCity(String cityQuery) {
        new FetchCityCoordinatesTask(cityQuery).execute();
    }
    
    /**
     * Hide keyboard
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    
    // ==================== AsyncTasks for City Search ====================
    
    /**
     * Search cities using OpenWeather Geocoding API
     */
    private class SearchCitiesTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {
            String query = params[0];
            List<String> results = new ArrayList<>();
            
            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String urlString = "http://api.openweathermap.org/geo/1.0/direct?q=" + 
                                   encodedQuery + "&limit=5&appid=" + API_KEY;
                
                android.util.Log.d("SearchCities", "URL: " + urlString);
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                
                int responseCode = connection.getResponseCode();
                android.util.Log.d("SearchCities", "Response code: " + responseCode);
                
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                reader.close();
                connection.disconnect();
                
                android.util.Log.d("SearchCities", "Response: " + response.toString());
                
                // Parse JSON response
                JSONArray jsonArray = new JSONArray(response.toString());
                android.util.Log.d("SearchCities", "JSON array length: " + jsonArray.length());
                
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
            android.util.Log.d("SearchCities", "Results count: " + results.size());
            for (String city : results) {
                android.util.Log.d("SearchCities", "City: " + city);
            }
            
            if (results.isEmpty()) {
                android.util.Log.e("SearchCities", "No results found!");
                Toast.makeText(MainActivity.this, "No cities found", Toast.LENGTH_SHORT).show();
                return;
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                MainActivity.this,
                R.layout.dropdown_item,
                results
            );
            etSearchCity.setAdapter(adapter);
            etSearchCity.showDropDown();
            
            android.util.Log.d("SearchCities", "Dropdown should be showing now");
        }
    }
    
    /**
     * Fetch coordinates for a selected city and load weather
     */
    private class FetchCityCoordinatesTask extends AsyncTask<Void, Void, Double[]> {
        private String cityQuery;
        private String cityName;
        
        public FetchCityCoordinatesTask(String cityQuery) {
            this.cityQuery = cityQuery;
        }
        
        @Override
        protected void onPreExecute() {
            showSkeletonLoading();
        }
        
        @Override
        protected Double[] doInBackground(Void... params) {
            try {
                String encodedQuery = URLEncoder.encode(cityQuery, "UTF-8");
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
                    
                    cityName = city.getString("name");
                    String country = city.optString("country", "");
                    if (!country.isEmpty()) {
                        cityName += ", " + country;
                    }
                    
                    double lat = city.getDouble("lat");
                    double lon = city.getDouble("lon");
                    
                    return new Double[]{lat, lon};
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(Double[] coords) {
            if (coords != null && coords.length == 2) {
                currentCity = cityName;
                currentLat = coords[0];
                currentLon = coords[1];
                
                // Fetch weather using coordinates
                fetchWeatherByCoordinates(currentLat, currentLon);
            } else {
                hideSkeletonLoading();
                MainActivity.this.showErrorState("City not found\nPlease try a different search", ErrorType.API_ERROR);
            }
        }
    }
    
    /**
     * Fetch weather by coordinates
     */
    private void fetchWeatherByCoordinates(double lat, double lon) {
        weatherApiClient.fetchWeatherByCoordinates(lat, lon, new WeatherApiClient.WeatherCallback() {
            @Override
            public void onSuccess(WeatherApiClient.WeatherData weatherData) {
                runOnUiThread(() -> {
                    hideSkeletonLoading();
                    displayWeatherData(weatherData);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    hideSkeletonLoading();
                    showErrorState(error, ErrorType.API_ERROR);
                });
            }
        });
    }
    
    // ==================== Forecast Methods ====================
    
    /**
     * Load 5-day forecast
     */
    private void loadForecast(String cityName) {
        weatherApiClient.fetchForecast(cityName, new WeatherApiClient.ForecastCallback() {
            @Override
            public void onSuccess(List<WeatherApiClient.ForecastDay> forecastList) {
                runOnUiThread(() -> displayForecast(forecastList));
            }
            
            @Override
            public void onError(String error) {
                android.util.Log.e("Forecast", "Error loading forecast: " + error);
                // Don't show error to user, just hide forecast section silently
            }
        });
    }
    
    /**
     * Display forecast data in UI
     */
    private void displayForecast(List<WeatherApiClient.ForecastDay> forecastList) {
        forecastContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        
        for (WeatherApiClient.ForecastDay forecast : forecastList) {
            View forecastItem = inflater.inflate(R.layout.item_forecast_day, forecastContainer, false);
            
            TextView tvDay = forecastItem.findViewById(R.id.tvForecastDay);
            ImageView ivIcon = forecastItem.findViewById(R.id.ivForecastIcon);
            TextView tvMaxTemp = forecastItem.findViewById(R.id.tvForecastMaxTemp);
            TextView tvMinTemp = forecastItem.findViewById(R.id.tvForecastMinTemp);
            
            tvDay.setText(forecast.dayName);
            ivIcon.setImageResource(WeatherApiClient.getWeatherIcon(forecast.weatherId));
            tvMaxTemp.setText(String.format(Locale.getDefault(), "%.0f°", forecast.maxTemp));
            tvMinTemp.setText(String.format(Locale.getDefault(), "%.0f°", forecast.minTemp));
            
            forecastContainer.addView(forecastItem);
        }
    }
}