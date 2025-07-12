package com.example.weatherapp.models;

public class DailyForecastModel {
    public String dayName;
    public float minTemp;
    public float maxTemp;
    public String iconCode;
    public String date;

    public DailyForecastModel(String dayName, float minTemp, float maxTemp, String iconCode, String date) {
        this.dayName = dayName;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.iconCode = iconCode;
        this.date = date;
    }
}
