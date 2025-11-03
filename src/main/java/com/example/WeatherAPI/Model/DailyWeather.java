package com.example.WeatherAPI.Model;

public class DailyWeather {
    private String date;
    private double maxTemperature;
    private double minTemperature;
    private double precipitationSum;
    private String sunrise;
    private String sunset;
    private double uvIndexMax;

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public double getPrecipitationSum() {
        return precipitationSum;
    }

    public void setPrecipitationSum(double precipitationSum) {
        this.precipitationSum = precipitationSum;
    }

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public double getUvIndexMax() {
        return uvIndexMax;
    }

    public void setUvIndexMax(double uvIndexMax) {
        this.uvIndexMax = uvIndexMax;
    }
}
