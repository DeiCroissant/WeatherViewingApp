package com.example.weatherviewingapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Weather API Client using OpenWeatherMap API
 * Uses HttpURLConnection (Java networking) + AsyncTask for background operations
 * 
 * Note: AsyncTask is deprecated in API 30+, but it's good for learning.
 * Alternative: Use Thread + Handler or modern solutions like Retrofit/Volley
 */
public class WeatherApiClient {
    
    private static final String TAG = "WeatherApiClient";
    
    // OpenWeatherMap API (Free tier)
    // NOTE: You need to register at https://openweathermap.org/api to get your own API key
    private static final String API_KEY = "c087fa97752f540e360b43023b2d945a"; // Replace with your API key
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    
    /**
     * Interface for callback when API call completes
     */
    public interface WeatherCallback {
        void onSuccess(WeatherData weatherData);
        void onError(String errorMessage);
    }
    
    /**
     * Data class to hold weather information - Enhanced with more details
     */
    public static class WeatherData {
        // Basic info
        public String cityName;
        public double temperature; // In Celsius
        public String condition;
        public String description;
        public int weatherId; // For icon mapping
        
        // Extended details
        public double feelsLike; // Feels like temperature
        public int humidity; // Humidity %
        public double windSpeed; // m/s
        public int windDeg; // Wind direction in degrees
        public int pressure; // hPa
        public int visibility; // meters
        public int clouds; // Cloudiness %
        public double uvIndex; // UV index (if available)
        
        // Sun times
        public long sunrise; // Unix timestamp
        public long sunset; // Unix timestamp
        
        // Rain/Snow
        public double rain1h; // Rain volume for last 1 hour (mm)
        public double snow1h; // Snow volume for last 1 hour (mm)
        
        public WeatherData(String cityName, double temperature, String condition, 
                          String description, int weatherId) {
            this.cityName = cityName;
            this.temperature = temperature;
            this.condition = condition;
            this.description = description;
            this.weatherId = weatherId;
        }
        
        /**
         * Convert Celsius to Fahrenheit
         */
        public double getTemperatureInFahrenheit() {
            return (temperature * 9/5) + 32;
        }
        
        /**
         * Get feels like in Fahrenheit
         */
        public double getFeelsLikeInFahrenheit() {
            return (feelsLike * 9/5) + 32;
        }
        
        /**
         * Get wind direction as string (N, NE, E, etc.)
         */
        public String getWindDirection() {
            String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
            int index = (int) ((windDeg + 22.5) / 45) % 8;
            return directions[index];
        }
        
        /**
         * Convert wind speed m/s to km/h
         */
        public double getWindSpeedKmh() {
            return windSpeed * 3.6;
        }
        
        /**
         * Get UV index level description
         */
        public String getUvIndexLevel() {
            if (uvIndex < 3) return "Thấp";
            if (uvIndex < 6) return "Trung bình";
            if (uvIndex < 8) return "Cao";
            if (uvIndex < 11) return "Rất cao";
            return "Cực cao";
        }
    }
    
    /**
     * Forecast data for a single day
     */
    public static class ForecastDay {
        public String date; // Date string (e.g., "2025-11-11")
        public String dayName; // Day name (e.g., "T2", "T3", "Hôm nay")
        public int weatherId; // Weather condition ID
        public double maxTemp; // Max temperature
        public double minTemp; // Min temperature
        public String description; // Weather description
        
        public ForecastDay(String date, String dayName, int weatherId, 
                          double maxTemp, double minTemp, String description) {
            this.date = date;
            this.dayName = dayName;
            this.weatherId = weatherId;
            this.maxTemp = maxTemp;
            this.minTemp = minTemp;
            this.description = description;
        }
    }
    
    /**
     * Callback for forecast data
     */
    public interface ForecastCallback {
        void onSuccess(List<ForecastDay> forecastList);
        void onError(String error);
    }
    
    /**
     * Fetch weather data for a city
     * @param cityName Name of the city
     * @param callback Callback to handle result
     */
    public void fetchWeather(String cityName, WeatherCallback callback) {
        new FetchWeatherTask(callback).execute(cityName);
    }
    
    /**
     * Fetch weather data by coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @param callback Callback to handle result
     */
    public void fetchWeatherByCoordinates(double lat, double lon, WeatherCallback callback) {
        new FetchWeatherByCoordinatesTask(callback).execute(lat, lon);
    }
    
    /**
     * Fetch 5-day forecast for a city
     * @param cityName Name of the city
     * @param callback Callback to handle result
     */
    public void fetchForecast(String cityName, ForecastCallback callback) {
        new FetchForecastTask(callback).execute(cityName);
    }
    
    /**
     * AsyncTask to perform network operation in background
     * Params: String (city name)
     * Progress: Void (no progress update)
     * Result: WeatherData or null
     */
    private static class FetchWeatherTask extends AsyncTask<String, Void, WeatherData> {
        
        private WeatherCallback callback;
        private String errorMessage;
        
        FetchWeatherTask(WeatherCallback callback) {
            this.callback = callback;
        }
        
        @Override
        protected WeatherData doInBackground(String... params) {
            if (params.length == 0) {
                errorMessage = "City name is required";
                return null;
            }
            
            String cityName = params[0];
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            
            try {
                // Build URL
                String urlString = BASE_URL + "?q=" + cityName + 
                                  "&appid=" + API_KEY + 
                                  "&units=metric" + // Use metric (Celsius)
                                  "&lang=vi"; // Vietnamese descriptions
                
                URL url = new URL(urlString);
                
                // Open connection
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000); // 10 seconds
                connection.setReadTimeout(10000);
                connection.connect();
                
                // Check response code
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    errorMessage = "HTTP Error: " + responseCode;
                    return null;
                }
                
                // Read response
                reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
                );
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                // Parse JSON
                return parseWeatherJson(response.toString());
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather", e);
                errorMessage = e.getMessage();
                return null;
                
            } finally {
                // Clean up
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing reader", e);
                    }
                }
            }
        }
        
        @Override
        protected void onPostExecute(WeatherData result) {
            if (result != null) {
                callback.onSuccess(result);
            } else {
                callback.onError(errorMessage != null ? errorMessage : "Unknown error");
            }
        }
        
        /**
         * Parse JSON response from OpenWeatherMap API - Enhanced with more details
         */
        private static WeatherData parseWeatherJson(String jsonString) throws JSONException {
            JSONObject json = new JSONObject(jsonString);
            
            // Basic info
            String cityName = json.getString("name");
            
            // Main weather data
            JSONObject main = json.getJSONObject("main");
            double temperature = main.getDouble("temp");
            double feelsLike = main.optDouble("feels_like", temperature);
            int humidity = main.optInt("humidity", 0);
            int pressure = main.optInt("pressure", 0);
            
            // Weather condition
            JSONArray weatherArray = json.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            String condition = weather.getString("main");
            String description = weather.getString("description");
            int weatherId = weather.getInt("id");
            
            // Wind data
            JSONObject wind = json.optJSONObject("wind");
            double windSpeed = wind != null ? wind.optDouble("speed", 0) : 0;
            int windDeg = wind != null ? wind.optInt("deg", 0) : 0;
            
            // Clouds
            JSONObject clouds = json.optJSONObject("clouds");
            int cloudiness = clouds != null ? clouds.optInt("all", 0) : 0;
            
            // Visibility
            int visibility = json.optInt("visibility", 0);
            
            // Sun times (sys object)
            JSONObject sys = json.optJSONObject("sys");
            long sunrise = sys != null ? sys.optLong("sunrise", 0) : 0;
            long sunset = sys != null ? sys.optLong("sunset", 0) : 0;
            
            // Rain (if exists)
            JSONObject rain = json.optJSONObject("rain");
            double rain1h = rain != null ? rain.optDouble("1h", 0) : 0;
            
            // Snow (if exists)
            JSONObject snow = json.optJSONObject("snow");
            double snow1h = snow != null ? snow.optDouble("1h", 0) : 0;
            
            // Create WeatherData object
            WeatherData data = new WeatherData(cityName, temperature, condition, description, weatherId);
            
            // Set extended details
            data.feelsLike = feelsLike;
            data.humidity = humidity;
            data.windSpeed = windSpeed;
            data.windDeg = windDeg;
            data.pressure = pressure;
            data.visibility = visibility;
            data.clouds = cloudiness;
            data.sunrise = sunrise;
            data.sunset = sunset;
            data.rain1h = rain1h;
            data.snow1h = snow1h;
            data.uvIndex = 0; // UV not in current weather API, need separate call
            
            return data;
        }
    }
    
    /**
     * Get weather icon resource based on weather ID
     * OpenWeatherMap weather condition IDs:
     * https://openweathermap.org/weather-conditions
     */
    public static int getWeatherIcon(int weatherId) {
        // Thunderstorm (200-232)
        if (weatherId >= 200 && weatherId < 300) {
            return R.drawable.ic_weather_thunderstorm;
        }
        // Drizzle (300-321) or Rain (500-531)
        else if ((weatherId >= 300 && weatherId < 400) || (weatherId >= 500 && weatherId < 600)) {
            return R.drawable.ic_weather_rain;
        }
        // Snow (600-622)
        else if (weatherId >= 600 && weatherId < 700) {
            return R.drawable.ic_weather_snow;
        }
        // Atmosphere (701-781) - fog, mist, etc.
        else if (weatherId >= 700 && weatherId < 800) {
            return R.drawable.ic_weather_mist;
        }
        // Clear (800)
        else if (weatherId == 800) {
            return R.drawable.ic_weather_clear;
        }
        // Clouds (801-804)
        else if (weatherId > 800) {
            return R.drawable.ic_weather_clouds;
        }
        
        return R.drawable.ic_weather_clear;
    }
    
    /**
     * Get background gradient based on weather and temperature
     */
    public static int getBackgroundGradient(int weatherId, double temperature, boolean isNight) {
        // Night time - use night gradient
        if (isNight) {
            return R.drawable.bg_gradient_night;
        }
        
        // Rain/Storm - use rain gradient
        if (weatherId >= 200 && weatherId < 600) {
            return R.drawable.bg_gradient_rain;
        }
        
        // Clouds
        if (weatherId > 800) {
            return R.drawable.bg_gradient_clouds;
        }
        
        // Clear - use temperature-based gradient
        if (temperature > 30) {
            return R.drawable.bg_gradient_hot;
        } else if (temperature < 15) {
            return R.drawable.bg_gradient_cold;
        } else {
            return R.drawable.bg_gradient_clear;
        }
    }    
    /**
     * AsyncTask to fetch weather by coordinates
     */
    private static class FetchWeatherByCoordinatesTask extends AsyncTask<Double, Void, WeatherData> {
        
        private WeatherCallback callback;
        private String errorMessage;
        
        FetchWeatherByCoordinatesTask(WeatherCallback callback) {
            this.callback = callback;
        }
        
        @Override
        protected WeatherData doInBackground(Double... params) {
            if (params.length < 2) {
                errorMessage = "Latitude and Longitude are required";
                return null;
            }
            
            double lat = params[0];
            double lon = params[1];
            
            try {
                // Build URL with coordinates
                String urlString = BASE_URL + "?lat=" + lat + "&lon=" + lon + 
                                   "&appid=" + API_KEY + 
                                   "&units=metric" + // Use metric (Celsius)
                                   "&lang=vi"; // Vietnamese descriptions
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    reader.close();
                    connection.disconnect();
                    
                    return FetchWeatherTask.parseWeatherJson(response.toString());
                } else if (responseCode == 404) {
                    errorMessage = "Location not found";
                } else if (responseCode == 401) {
                    errorMessage = "Invalid API key";
                } else {
                    errorMessage = "Server error: " + responseCode;
                }
                
            } catch (IOException e) {
                errorMessage = "Network error: " + e.getMessage();
            } catch (Exception e) {
                errorMessage = "Error: " + e.getMessage();
            }
            
            return null;
        }
        
        @Override
        protected void onPostExecute(WeatherData weatherData) {
            if (weatherData != null) {
                callback.onSuccess(weatherData);
            } else {
                callback.onError(errorMessage != null ? errorMessage : "Unknown error");
            }
        }
    }
    
    /**
     * AsyncTask to fetch 5-day weather forecast
     */
    private class FetchForecastTask extends AsyncTask<String, Void, List<ForecastDay>> {
        private ForecastCallback callback;
        private String errorMessage;
        
        public FetchForecastTask(ForecastCallback callback) {
            this.callback = callback;
        }
        
        @Override
        protected List<ForecastDay> doInBackground(String... params) {
            if (params.length == 0) {
                errorMessage = "City name is required";
                return null;
            }
            
            String cityName = params[0];
            List<ForecastDay> forecastList = new ArrayList<>();
            
            try {
                // Use forecast API endpoint
                String urlString = "https://api.openweathermap.org/data/2.5/forecast?q=" + cityName + 
                                   "&appid=" + API_KEY + 
                                   "&units=metric" + 
                                   "&lang=vi";
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    reader.close();
                    connection.disconnect();
                    
                    // Parse forecast JSON
                    forecastList = parseForecastJson(response.toString());
                    
                } else {
                    errorMessage = "HTTP Error: " + responseCode;
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = "Error: " + e.getMessage();
            }
            
            return forecastList;
        }
        
        @Override
        protected void onPostExecute(List<ForecastDay> forecastList) {
            if (forecastList != null && !forecastList.isEmpty()) {
                callback.onSuccess(forecastList);
            } else {
                callback.onError(errorMessage != null ? errorMessage : "No forecast data available");
            }
        }
    }
    
    /**
     * Parse forecast JSON and group by day (get daily min/max)
     */
    private static List<ForecastDay> parseForecastJson(String jsonString) throws JSONException {
        List<ForecastDay> forecastList = new ArrayList<>();
        Map<String, DayData> dayMap = new HashMap<>();
        
        JSONObject root = new JSONObject(jsonString);
        JSONArray list = root.getJSONArray("list");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
        Calendar calendar = Calendar.getInstance();
        
        // Process each 3-hour forecast entry
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);
            
            // Get date
            String dateTime = item.getString("dt_txt"); // "2025-11-10 12:00:00"
            String date = dateTime.split(" ")[0]; // "2025-11-10"
            
            // Get temperature
            JSONObject main = item.getJSONObject("main");
            double temp = main.getDouble("temp");
            
            // Get weather info
            JSONArray weather = item.getJSONArray("weather");
            JSONObject weatherObj = weather.getJSONObject(0);
            int weatherId = weatherObj.getInt("id");
            String description = weatherObj.getString("description");
            
            // Group by day
            if (!dayMap.containsKey(date)) {
                dayMap.put(date, new DayData());
            }
            
            DayData dayData = dayMap.get(date);
            dayData.addTemp(temp);
            dayData.weatherId = weatherId; // Use first entry's weather
            dayData.description = description;
        }
        
        // Convert to ForecastDay list (max 5 days)
        int count = 0;
        for (String date : dayMap.keySet()) {
            if (count >= 5) break;
            
            DayData dayData = dayMap.get(date);
            
            // Get day name
            String dayName;
            if (count == 0) {
                dayName = "Hôm nay";
            } else if (count == 1) {
                dayName = "Ngày mai";
            } else {
                // Get day of week (T2, T3, T4, T5, T6, T7, CN)
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    calendar.setTime(sdf.parse(date));
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    String[] days = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
                    dayName = days[dayOfWeek - 1];
                } catch (Exception e) {
                    dayName = date;
                }
            }
            
            forecastList.add(new ForecastDay(
                date,
                dayName,
                dayData.weatherId,
                dayData.maxTemp,
                dayData.minTemp,
                dayData.description
            ));
            
            count++;
        }
        
        return forecastList;
    }
    
    /**
     * Helper class to store daily temperature data
     */
    private static class DayData {
        double maxTemp = Double.MIN_VALUE;
        double minTemp = Double.MAX_VALUE;
        int weatherId = 800;
        String description = "";
        
        void addTemp(double temp) {
            if (temp > maxTemp) maxTemp = temp;
            if (temp < minTemp) minTemp = temp;
        }
    }
}
