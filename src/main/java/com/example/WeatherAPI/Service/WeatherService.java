package com.example.WeatherAPI.Service;

import com.example.WeatherAPI.Model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

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

            // Build current weather object
            CurrentWeather currentWeather = new CurrentWeather();
            currentWeather.setTemperature(current.getDouble("temperature"));
            currentWeather.setWindspeed(current.getDouble("windspeed"));
            currentWeather.setWinddirection(current.getDouble("winddirection"));
            currentWeather.setWeathercode(current.getInt("weathercode"));
            currentWeather.setTime(current.getString("time"));

            // Parse hourly data
            JSONArray hourlyTimes = hourly.getJSONArray("time");
            JSONArray temps = hourly.getJSONArray("temperature_2m");
            JSONArray humidity = hourly.getJSONArray("relative_humidity_2m");
            JSONArray feelsLike = hourly.getJSONArray("apparent_temperature");
            JSONArray precip = hourly.getJSONArray("precipitation");
            JSONArray clouds = hourly.getJSONArray("cloud_cover");
            JSONArray wind = hourly.getJSONArray("windspeed_10m");
            JSONArray vis = hourly.getJSONArray("visibility");

            List<HourlyWeather> hourlyList = new ArrayList<>();
            for (int i = 0; i < hourlyTimes.length(); i++) {
                HourlyWeather h = new HourlyWeather();
                h.setTime(hourlyTimes.getString(i));
                h.setTemperature(temps.getDouble(i));
                h.setHumidity(humidity.getDouble(i));
                h.setApparentTemperature(feelsLike.getDouble(i));
                h.setPrecipitation(precip.getDouble(i));
                h.setCloudCover(clouds.getDouble(i));
                h.setWindspeed(wind.getDouble(i));
                h.setVisibility(vis.getDouble(i));
                hourlyList.add(h);
            }

            // Parse daily data
            JSONArray dailyTimes = daily.getJSONArray("time");
            JSONArray maxTemp = daily.getJSONArray("temperature_2m_max");
            JSONArray minTemp = daily.getJSONArray("temperature_2m_min");
            JSONArray precipSum = daily.getJSONArray("precipitation_sum");
            JSONArray sunrise = daily.getJSONArray("sunrise");
            JSONArray sunset = daily.getJSONArray("sunset");
            JSONArray uvMax = daily.getJSONArray("uv_index_max");

            List<DailyWeather> dailyList = new ArrayList<>();
            for (int i = 0; i < dailyTimes.length(); i++) {
                DailyWeather d = new DailyWeather();
                d.setDate(dailyTimes.getString(i));
                d.setMaxTemperature(maxTemp.getDouble(i));
                d.setMinTemperature(minTemp.getDouble(i));
                d.setPrecipitationSum(precipSum.getDouble(i));
                d.setSunrise(sunrise.getString(i));
                d.setSunset(sunset.getString(i));
                d.setUvIndexMax(uvMax.getDouble(i));
                dailyList.add(d);
            }

            // Step 3: Build WeatherResponse
            WeatherResponse weather = new WeatherResponse();
            weather.setCity(city);
            weather.setCountry(country);
            weather.setLatitude(latitude);
            weather.setLongitude(longitude);
            weather.setCurrent(currentWeather);
            weather.setHourly(hourlyList);
            weather.setDaily(dailyList);

            return weather;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data: " + e.getMessage());
        }
    }
}
