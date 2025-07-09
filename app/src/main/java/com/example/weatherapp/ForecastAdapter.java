package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private List<ForecastModel.ForecastItem> forecastList;

    public ForecastAdapter(List<ForecastModel.ForecastItem> forecastList) {
        this.forecastList = forecastList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTime, tvTemp, tvWeather;

        public ViewHolder(View view) {
            super(view);
            tvTime = view.findViewById(R.id.tvTime);
            tvTemp = view.findViewById(R.id.tvTemp);
            tvWeather = view.findViewById(R.id.tvWeather);
        }
    }

    @Override
    public ForecastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.forecast_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ForecastModel.ForecastItem item = forecastList.get(position);
        holder.tvTime.setText(item.dt_txt);
        holder.tvTemp.setText(item.main.temp + "°C");
        holder.tvWeather.setText(item.weather[0].description);
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }
}
