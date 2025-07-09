package com.example.weatherapp;

public class WeatherModel {
    public Main main;
    public Weather[] weather;
    public String name; // city name

    public class Main {
        public float temp;
        public int humidity;
    }

    public class Weather {
        public String main;
        public String description;
    }
}
