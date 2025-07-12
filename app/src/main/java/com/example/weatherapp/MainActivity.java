package com.example.weatherapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.*;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.adapters.DailyForecastAdapter;
import com.example.weatherapp.models.DailyForecastModel;
import com.example.weatherapp.models.ForecastModel;
import com.example.weatherapp.models.WeatherModel;
import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    EditText etCityName;
    Button btnGetWeather, btnLocationWeather, btnLogout;
    TextView tvResult, tvSuggestion;
    RecyclerView rvForecast;

    private final String API_KEY = "c8ee6647f006a3ce3f6d5e41aeb2d4d4";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        etCityName = findViewById(R.id.etCityName);
        btnGetWeather = findViewById(R.id.btnGetWeather);
        btnLocationWeather = findViewById(R.id.btnLocationWeather);
        tvResult = findViewById(R.id.tvResult);
        tvSuggestion = findViewById(R.id.tvSuggestion);
        btnLogout = findViewById(R.id.btnLogout);
        rvForecast = findViewById(R.id.rvForecast);
        rvForecast.setLayoutManager(new LinearLayoutManager(this));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Switch switchTheme = findViewById(R.id.switchTheme);
        switchTheme.setChecked(isDark);
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        String lastCity = prefs.getString("last_city", null);
        float lastLat = prefs.getFloat("last_lat", -1f);
        float lastLon = prefs.getFloat("last_lon", -1f);

        if (lastCity != null) {
            fetchWeather(lastCity);
        } else if (lastLat != -1 && lastLon != -1) {
            fetchWeatherByCoordinates(lastLat, lastLon);
        }

        scheduleDailyWeatherNotification();

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        btnGetWeather.setOnClickListener(v -> {
            String city = etCityName.getText().toString().trim();
            if (city.isEmpty()) {
                Toast.makeText(MainActivity.this, "Enter a city name", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("last_city", city);
            editor.remove("last_lat");
            editor.remove("last_lon");
            editor.apply();
            fetchWeather(city);
        });

        btnLocationWeather.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    handleLocation(location);
                } else {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                            .addOnSuccessListener(this::handleLocation);
                }
            });
        });
    }

    private List<DailyForecastModel> groupForecastIntoDailyList(List<ForecastModel.ForecastItem> forecastList) {
        Map<String, List<ForecastModel.ForecastItem>> groupedMap = new LinkedHashMap<>();

        for (ForecastModel.ForecastItem item : forecastList) {
            String date = item.dt_txt.split(" ")[0];
            groupedMap.computeIfAbsent(date, k -> new ArrayList<>()).add(item);
        }

        List<DailyForecastModel> dailyList = new ArrayList<>();
        int count = 0;

        for (Map.Entry<String, List<ForecastModel.ForecastItem>> entry : groupedMap.entrySet()) {
            if (count++ == 5) break;

            List<ForecastModel.ForecastItem> items = entry.getValue();
            float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
            String iconCode = "";

            for (ForecastModel.ForecastItem fi : items) {
                float temp = (float) fi.main.temp;
                min = Math.min(min, temp);
                max = Math.max(max, temp);
            }

            if (!items.isEmpty() && items.get(0).weather != null && items.get(0).weather.length > 0 && items.get(0).weather[0] != null) {
                iconCode = items.get(0).weather[0].icon;
            }


            String dayName;
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = format.parse(entry.getKey());
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                dayName = dayFormat.format(date);
            } catch (ParseException e) {
                dayName = entry.getKey();
            }

            dailyList.add(new DailyForecastModel(dayName, min, max, iconCode, entry.getKey()));
        }

        return dailyList;
    }

    private void fetchWeather(String city) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric";
        String forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + API_KEY + "&units=metric";
        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(new StringRequest(Request.Method.GET, url, response -> {
            WeatherModel model = new Gson().fromJson(response, WeatherModel.class);
            tvResult.setText("City: " + model.name + "\nTemperature: " + model.main.temp + "°C\nHumidity: " + model.main.humidity + "%");
            tvSuggestion.setText(getSuggestion(model.main.temp, model.weather[0].main));
        }, error -> {
            tvResult.setText("Error fetching data!");
            tvSuggestion.setText("Unable to generate suggestion.");
        }));

        queue.add(new StringRequest(Request.Method.GET, forecastUrl, response -> {
            ForecastModel forecastModel = new Gson().fromJson(response, ForecastModel.class);
            Global.forecastList = forecastModel.list;
            List<DailyForecastModel> dailySummaries = groupForecastIntoDailyList(forecastModel.list);
            rvForecast.setAdapter(new DailyForecastAdapter(MainActivity.this, dailySummaries));
        }, error -> Toast.makeText(this, "Forecast fetch failed!", Toast.LENGTH_SHORT).show()));
    }

    private void fetchWeatherByCoordinates(double lat, double lon) {
        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putFloat("last_lat", (float) lat);
        editor.putFloat("last_lon", (float) lon);
        editor.remove("last_city");
        editor.apply();

        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=metric";
        String forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=metric";

        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(new StringRequest(Request.Method.GET, url, response -> {
            WeatherModel model = new Gson().fromJson(response, WeatherModel.class);
            tvResult.setText("City: " + model.name + "\nTemperature: " + model.main.temp + "°C\nHumidity: " + model.main.humidity + "%");
            tvSuggestion.setText(getSuggestion(model.main.temp, model.weather[0].main));
        }, error -> {
            tvResult.setText("Error fetching location weather!");
            tvSuggestion.setText("Unable to generate suggestion.");
        }));

        queue.add(new StringRequest(Request.Method.GET, forecastUrl, response -> {
            ForecastModel forecastModel = new Gson().fromJson(response, ForecastModel.class);
            Global.forecastList = forecastModel.list;
            List<DailyForecastModel> dailySummaries = groupForecastIntoDailyList(forecastModel.list);
            rvForecast.setAdapter(new DailyForecastAdapter(MainActivity.this, dailySummaries));
        }, error -> Toast.makeText(this, "Location forecast fetch failed!", Toast.LENGTH_SHORT).show()));
    }

    private void handleLocation(Location location) {
        fetchWeatherByCoordinates(location.getLatitude(), location.getLongitude());
    }

    private String getSuggestion(float temp, String condition) {
        StringBuilder suggestion = new StringBuilder();

        switch (condition.toLowerCase()) {
            case "rain": suggestion.append("☔ It's raining. Keep an umbrella and waterproof shoes.\n"); break;
            case "clear": suggestion.append("☀️ Clear skies. Perfect for outdoor plans!\n"); break;
            case "snow": suggestion.append("❄️ Snow outside. Roads may be slippery. Wear boots.\n"); break;
            case "clouds": suggestion.append("⛅ Overcast skies. It might feel gloomy — keep a light jacket.\n"); break;
            case "thunderstorm": suggestion.append("🌩️ Thunderstorm warning. Stay indoors for safety.\n"); break;
            case "drizzle": suggestion.append("🌦️ Light drizzle expected. A raincoat might help.\n"); break;
            case "fog": case "mist": suggestion.append("🌫️ Foggy outside. Drive carefully with lights on.\n"); break;
            case "haze": case "smoke": suggestion.append("🌁 Low air quality. Avoid long exposure outdoors.\n"); break;
            default: suggestion.append("🌤️ Mixed weather. Stay updated on hourly changes.\n"); break;
        }

        if (temp < 0) suggestion.append("🌡️ Temp: " + temp + "°C. Freezing! Wear thermal layers.");
        else if (temp < 5) suggestion.append("🌡️ Temp: " + temp + "°C. Very cold — bundle up!");
        else if (temp < 15) suggestion.append("🌡️ Temp: " + temp + "°C. Chilly — carry a jacket.");
        else if (temp < 25) suggestion.append("🌡️ Temp: " + temp + "°C. Pleasant — light clothes will work.");
        else if (temp < 32) suggestion.append("🌡️ Temp: " + temp + "°C. Warm — stay cool and hydrated.");
        else suggestion.append("🌡️ Temp: " + temp + "°C. Very hot — avoid staying out long.");

        return suggestion.toString();
    }

    private void scheduleDailyWeatherNotification() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
