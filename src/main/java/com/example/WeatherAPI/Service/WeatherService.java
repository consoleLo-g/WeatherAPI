package com.example.WeatherAPI.Service;

import com.example.WeatherAPI.Model.WeatherResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

@Service
public class WeatherService {

    public WeatherResponse getWeather(double latitude, double longitude) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Step 1: Reverse geocode to get city and country
            String geoUrl = String.format(
                    "https://api-bdc.io/data/reverse-geocode-client?latitude=%f&longitude=%f&localityLanguage=en",
                    latitude, longitude);
            String geoResponse = restTemplate.getForObject(geoUrl, String.class);
            JSONObject geoJson = new JSONObject(geoResponse);

            String city = geoJson.optString("city", "Unknown");
            String country = geoJson.optString("countryName", "Unknown");

            // Step 2: Fetch complete weather data (current + hourly + daily forecast)
            String weatherUrl = String.format(
                    "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f"
                            + "&current_weather=true"
                            + "&hourly=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,cloud_cover,visibility,windspeed_10m"
                            + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,sunrise,sunset,uv_index_max"
                            + "&timezone=auto",
                    latitude, longitude);
            String weatherResponse = restTemplate.getForObject(weatherUrl, String.class);
            JSONObject weatherJson = new JSONObject(weatherResponse);

            // Current weather
            JSONObject current = weatherJson.getJSONObject("current_weather");

            // Hourly and daily data
            JSONObject hourly = weatherJson.getJSONObject("hourly");
            JSONObject daily = weatherJson.getJSONObject("daily");

            // Step 3: Build WeatherResponse
            WeatherResponse weather = new WeatherResponse();
            weather.setCity(city);
            weather.setCountry(country);
            weather.setLatitude(latitude);
            weather.setLongitude(longitude);
            weather.setTemperature(current.getDouble("temperature"));
            weather.setWindspeed(current.getDouble("windspeed"));
            weather.setTime(current.getString("time"));
            weather.setHourly(hourly.toString());
            weather.setDaily(daily.toString());

            return weather;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data: " + e.getMessage());
        }
    }
}
