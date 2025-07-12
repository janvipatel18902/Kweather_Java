package com.example.weatherapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.adapters.ForecastAdapter;
import com.example.weatherapp.models.ForecastModel;

import java.util.ArrayList;
import java.util.List;

public class HourlyForecastActivity extends AppCompatActivity {
    RecyclerView rvHourly;
    ForecastAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_forecast);

        rvHourly = findViewById(R.id.rvHourly);
        rvHourly.setLayoutManager(new LinearLayoutManager(this));

        String selectedDate = getIntent().getStringExtra("selected_date");

        // Filter the forecast list by selected date
        List<ForecastModel.ForecastItem> filtered = new ArrayList<>();
        for (ForecastModel.ForecastItem item : Global.forecastList) {
            if (item.dt_txt.startsWith(selectedDate)) {
                filtered.add(item);
            }
        }

        adapter = new ForecastAdapter(filtered);
        rvHourly.setAdapter(adapter);
    }
}
