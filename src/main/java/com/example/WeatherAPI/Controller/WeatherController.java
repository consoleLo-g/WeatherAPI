package com.example.WeatherAPI.Controller;

import com.example.WeatherAPI.Model.WeatherResponse;
import com.example.WeatherAPI.Service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping
    public WeatherResponse getWeather(
            @RequestParam double lat,
            @RequestParam double lon) {
        return weatherService.getWeather(lat, lon);
    }
}
