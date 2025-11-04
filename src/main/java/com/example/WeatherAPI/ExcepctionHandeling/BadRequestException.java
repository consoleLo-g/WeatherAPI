package com.example.WeatherAPI.ExcepctionHandeling;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String messsage) {
        super(messsage);
    }
}
