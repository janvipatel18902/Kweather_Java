package com.example.weatherapp;

import java.util.List;

public class ForecastModel {
    public List<ForecastItem> list;

    public class ForecastItem {
        public Main main;
        public Weather[] weather;
        public String dt_txt;

        public class Main {
            public float temp;
            public int humidity;
        }

        public class Weather {
            public String main;
            public String description;
            public String icon;
        }
    }
}
