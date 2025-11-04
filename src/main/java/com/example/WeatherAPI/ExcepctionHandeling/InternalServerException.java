package com.example.WeatherAPI.ExcepctionHandeling;

public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }
}
