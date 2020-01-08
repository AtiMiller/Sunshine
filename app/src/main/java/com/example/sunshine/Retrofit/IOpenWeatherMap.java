package com.example.sunshine.Retrofit;

import com.example.sunshine.Model.WeatherForecastResult;
import com.example.sunshine.Model.WeatherResult;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IOpenWeatherMap {
    @GET("weather")
    Observable<WeatherResult> getWeatherByLatLon(@Query("lat")String lat,
                                                 @Query("lon")String lng,
                                                 @Query("appid")String appid,
                                                 @Query("units")String units);

    @GET("forecast")
    Observable<WeatherForecastResult> getWeatherForecastByLatLon(@Query("lat")String lat,
                                                                 @Query("lon")String lng,
                                                                 @Query("appid")String appid,
                                                                 @Query("units")String units);
}
