package com.example.weatheraqiplus

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather")
    fun getWeatherData(
        @Query("q") city:String,
        @Query("appid") appid:String,
        @Query("units") units:String
    ):Call<WeatherApp>

    @GET("geo/1.0/direct")
    fun getGeoLocation(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): Call<List<GeoLocation>>

    @GET("data/2.5/air_pollution")
    fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Call<AirQuality>
}

