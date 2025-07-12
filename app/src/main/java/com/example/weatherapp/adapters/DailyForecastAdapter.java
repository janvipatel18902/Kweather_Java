package com.example.weatherapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.HourlyForecastActivity;
import com.example.weatherapp.R;
import com.example.weatherapp.models.DailyForecastModel;

import java.util.List;

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.ViewHolder> {
    private final List<DailyForecastModel> forecastList;
    private final Context context;

    public DailyForecastAdapter(Context context, List<DailyForecastModel> forecastList) {
        this.context = context;
        this.forecastList = forecastList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvTempRange;
        ImageView ivIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvTempRange = itemView.findViewById(R.id.tvTempRange);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daily_forecast_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DailyForecastModel item = forecastList.get(position);
        holder.tvDay.setText(item.dayName);
        holder.tvTempRange.setText(String.format("%.0f° / %.0f°", item.maxTemp, item.minTemp));

        // ✅ Load weather icon if not null
        if (item.iconCode != null && !item.iconCode.isEmpty()) {
            String iconUrl = "https://openweathermap.org/img/wn/" + item.iconCode + "@2x.png";
            Glide.with(context).load(iconUrl).into(holder.ivIcon);
        } else {
            holder.ivIcon.setImageDrawable(null);  // Optional: clear image if no icon
        }

        // 📅 OnClick: go to hourly forecast screen
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HourlyForecastActivity.class);
            intent.putExtra("selected_date", item.date);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }
}
