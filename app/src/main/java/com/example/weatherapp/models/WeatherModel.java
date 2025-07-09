package com.example.weatherapp.models;

public class WeatherModel {
    public String name;
    public Main main;
    public Weather[] weather;

    public static class Main {
        public float temp;
        public int humidity;
    }

    public static class Weather {
        public String main;
    }
}

