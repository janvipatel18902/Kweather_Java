package com.example.weatherapp.models;

import java.util.List;

public class ForecastModel {
    public List<ForecastItem> list;

    public static class ForecastItem {
        public Main main;
        public Weather[] weather;
        public String dt_txt;

        public static class Main {
            public float temp;
        }

        public static class Weather {
            public String main;
        }
    }
}

