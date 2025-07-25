package com.example.weatherapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.models.ForecastModel;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {
    private final List<ForecastModel.ForecastItem> forecastList;

    public ForecastAdapter(List<ForecastModel.ForecastItem> forecastList) {
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.forecast_item, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastModel.ForecastItem item = forecastList.get(position);
        holder.tvTemp.setText("Temp: " + item.main.temp + "°C");
        holder.tvWeather.setText("Weather: " + item.weather[0].main);
        holder.tvTime.setText(item.dt_txt);

        // Load icon using Glide
        String iconCode = item.weather[0].icon;
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        Glide.with(holder.itemView.getContext()).load(iconUrl).into(holder.ivHourlyIcon);
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvTemp, tvWeather, tvTime;
        ImageView ivHourlyIcon;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvWeather = itemView.findViewById(R.id.tvWeather);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivHourlyIcon = itemView.findViewById(R.id.ivHourlyIcon); // icon view
        }
    }
}
