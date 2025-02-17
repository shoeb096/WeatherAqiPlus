package com.example.weatheraqiplus


data class GeoLocation(val lat: Double, val lon: Double)


data class AirQuality(
    val list: List<AirQualityData>
)

data class AirQualityData(
    val components: AirQualityComponents
)

data class AirQualityComponents(
    val pm2_5: Double // Fine particulate matter (PM2.5)
)