// package com.example.WeatherAPI.Service;

// import java.util.Map;

// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import com.example.WeatherAPI.Model.WaetherResponse;

// @Service
// public class WeatherService {
// private final RestTemplate restTemplate = new RestTemplate();
// private final String apiKey = "YOUR_API_KEY";

// public WaetherResponse getWeather(String city) {
// String url = "https://api.openweathermap.org/data/2.5/weather?q="
// + city + "&appid=" + apiKey + "&units=metric";
// Map<String, Object> response = restTemplate.getForObject(url, Map.class);

// WaetherResponse weather = new WaetherResponse();
// weather.setCity(city);
// Map<String, Object> main = (Map<String, Object>) response.get("main");
// weather.setTemperature((Double) main.get("temp"));
// weather.setHumidity((Double) main.get("humidity"));

// Map<String, Object> weatherDetails = ((List<Map<String, Object>>)
// response.get("weather")).get(0);
// weather.setDescription((String) weatherDetails.get("description"));

// return weather;
// }
// }
