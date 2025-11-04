package com.example.WeatherAPI.Service;

import com.example.WeatherAPI.Model.*;
import com.example.WeatherAPI.ExcepctionHandeling.BadRequestException;
import com.example.WeatherAPI.ExcepctionHandeling.ResourceNotFoundException;
import com.example.WeatherAPI.ExcepctionHandeling.InternalServerException;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    public WeatherResponse getWeather(double latitude, double longitude) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // 🧭 Validate input
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                logger.warn("Invalid latitude or longitude: lat={}, lon={}", latitude, longitude);
                throw new BadRequestException("Invalid latitude or longitude values.");
            }

            logger.info("Fetching weather data for coordinates: latitude={}, longitude={}", latitude, longitude);

            // 🌍 Step 1: Reverse geocode to get city and country
            String geoUrl = String.format(
                    "https://api-bdc.io/data/reverse-geocode-client?latitude=%f&longitude=%f&localityLanguage=en",
                    latitude, longitude);
            logger.debug("Reverse geocoding URL: {}", geoUrl);

            String geoResponse;
            try {
                geoResponse = restTemplate.getForObject(geoUrl, String.class);
            } catch (RestClientException e) {
                logger.error("Reverse geocoding API call failed: {}", e.getMessage());
                throw new InternalServerException("Failed to connect to reverse geocoding service.");
            }

            if (geoResponse == null || geoResponse.isEmpty()) {
                logger.error("Empty response from reverse geocode API");
                throw new InternalServerException("Empty response from reverse geocode API.");
            }

            JSONObject geoJson = new JSONObject(geoResponse);
            String city = geoJson.optString("city", "");
            String country = geoJson.optString("countryName", "");

            if (city.isEmpty()) {
                logger.warn("City not found for coordinates: lat={}, lon={}", latitude, longitude);
                throw new ResourceNotFoundException("City not found for the given coordinates.");
            }

            logger.info("Resolved location: City='{}', Country='{}'", city, country);

            // ☁️ Step 2: Fetch weather data (current + hourly + daily)
            String weatherUrl = String.format(
                    "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f"
                            + "&current_weather=true"
                            + "&hourly=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,cloud_cover,visibility,windspeed_10m"
                            + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,sunrise,sunset,uv_index_max"
                            + "&timezone=auto",
                    latitude, longitude);
            logger.debug("Weather API URL: {}", weatherUrl);

            String weatherResponse;
            try {
                weatherResponse = restTemplate.getForObject(weatherUrl, String.class);
            } catch (RestClientException e) {
                logger.error("Weather API call failed: {}", e.getMessage());
                throw new InternalServerException("Failed to connect to weather API service.");
            }

            if (weatherResponse == null || weatherResponse.isEmpty()) {
                logger.error("Empty response from weather API");
                throw new InternalServerException("Empty response from weather API.");
            }

            JSONObject weatherJson = new JSONObject(weatherResponse);
            logger.info("Successfully fetched weather JSON data.");

            // 🌡️ Current weather
            if (!weatherJson.has("current_weather")) {
                throw new ResourceNotFoundException("Current weather data not available for the given location.");
            }

            JSONObject current = weatherJson.getJSONObject("current_weather");

            // ☀️ Hourly and daily data
            JSONObject hourly = weatherJson.optJSONObject("hourly");
            JSONObject daily = weatherJson.optJSONObject("daily");

            if (hourly == null || daily == null) {
                throw new ResourceNotFoundException("Hourly or daily forecast data not found.");
            }

            // Build current weather object
            CurrentWeather currentWeather = new CurrentWeather();
            currentWeather.setTemperature(current.getDouble("temperature"));
            currentWeather.setWindspeed(current.getDouble("windspeed"));
            currentWeather.setWinddirection(current.getDouble("winddirection"));
            currentWeather.setWeathercode(current.getInt("weathercode"));
            currentWeather.setTime(current.getString("time"));

            logger.debug("Parsed current weather: {}", currentWeather);

            // 🌤️ Parse hourly data
            List<HourlyWeather> hourlyList = new ArrayList<>();
            JSONArray hourlyTimes = hourly.getJSONArray("time");
            JSONArray temps = hourly.getJSONArray("temperature_2m");
            JSONArray humidity = hourly.getJSONArray("relative_humidity_2m");
            JSONArray feelsLike = hourly.getJSONArray("apparent_temperature");
            JSONArray precip = hourly.getJSONArray("precipitation");
            JSONArray clouds = hourly.getJSONArray("cloud_cover");
            JSONArray wind = hourly.getJSONArray("windspeed_10m");
            JSONArray vis = hourly.getJSONArray("visibility");

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

            logger.debug("Parsed {} hourly weather entries.", hourlyList.size());

            // 🌅 Parse daily data
            List<DailyWeather> dailyList = new ArrayList<>();
            JSONArray dailyTimes = daily.getJSONArray("time");
            JSONArray maxTemp = daily.getJSONArray("temperature_2m_max");
            JSONArray minTemp = daily.getJSONArray("temperature_2m_min");
            JSONArray precipSum = daily.getJSONArray("precipitation_sum");
            JSONArray sunrise = daily.getJSONArray("sunrise");
            JSONArray sunset = daily.getJSONArray("sunset");
            JSONArray uvMax = daily.getJSONArray("uv_index_max");

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

            logger.debug("Parsed {} daily weather entries.", dailyList.size());

            // 🌎 Step 3: Build final WeatherResponse
            WeatherResponse weather = new WeatherResponse();
            weather.setCity(city);
            weather.setCountry(country);
            weather.setLatitude(latitude);
            weather.setLongitude(longitude);
            weather.setCurrent(currentWeather);
            weather.setHourly(hourlyList);
            weather.setDaily(dailyList);

            logger.info("Weather data successfully built for {} ({})", city, country);
            return weather;

        } catch (BadRequestException | ResourceNotFoundException | InternalServerException e) {
            logger.warn("Handled exception: {}", e.getMessage());
            throw e; // Let GlobalExceptionHandler handle it
        } catch (Exception e) {
            logger.error("Unexpected error occurred: {}", e.getMessage(), e);
            throw new InternalServerException("Unexpected internal error occurred while fetching weather data.");
        }
    }
}
