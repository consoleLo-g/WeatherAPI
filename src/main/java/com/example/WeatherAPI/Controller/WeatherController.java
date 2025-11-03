package com.example.WeatherAPI.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.WeatherAPI.Model.WeatherResponse;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    @GetMapping("/{city}")
    public WeatherResponse getWeather(@PathVariable String city) {
        WeatherResponse weather = new WeatherResponse();
        weather.setCity(city);
        weather.setTemperature(28.5);
        weather.setDescription("Clear sky");
        weather.setHumidity(65);
        return weather;
    }
}