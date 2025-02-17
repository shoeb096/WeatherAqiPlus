package com.example.weatheraqiplus

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatheraqiplus.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import kotlin.math.roundToInt
import java.util.Locale

// api key : eacc1aee25aacd65c56be05821cc4cce
class MainActivity : AppCompatActivity() {

private val binding: ActivityMainBinding by lazy {
    ActivityMainBinding.inflate(layoutInflater)
}



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        fetchWeatherData("Lucknow")
        fetchAirQuality("Lucknow")
        searchCity()


        binding.searchView.setOnClickListener {
            binding.searchView.isIconified = false  // Ensures full expansion when clicked
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun searchCity() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    fetchWeatherData(it)  // Ensure this function is implemented
                    fetchAirQuality(it)  // Fetch air quality for entered city
                    binding.searchView.clearFocus() // Hide keyboard after submission
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }


    private fun fetchWeatherData(locationText:String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(locationText,"eacc1aee25aacd65c56be05821cc4cce","metric")
        response.enqueue(object :Callback<WeatherApp>{


            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperature = responseBody.main.temp.roundToInt()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunRise = responseBody.sys.sunrise * 1000L // Convert to milliseconds
                    val sunSet = responseBody.sys.sunset * 1000L   // Convert to milliseconds
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
                    val maxTemp = responseBody.main.temp_max.roundToInt()

                    binding.tempText.text = "$temperature °C"
                    binding.weatherCondition.text = condition
                    binding.minMaxTemp.text = "Max Temp :$maxTemp °C"
                    binding.humidityText.text = "$humidity %"
                    binding.windText.text = "$windSpeed m/s"
                    binding.sunriseText.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sunRise))
                    binding.sunsetText.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(sunSet))
                    binding.rainText.text = condition
                    binding.locationText.text = locationText
                    binding.dateText.text = date()

                    // Pass sunrise & sunset times to changeImages function
                   // changeImages(condition, sunRise, sunSet)
                    changeImages(condition, responseBody.sys.sunrise, responseBody.sys.sunset)
                }
            }


            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })

    }



    private fun changeImages(condition: String, sunrise: Int, sunset: Int) {
        val isNight = isNightTime(sunrise, sunset) // Call function to check if it's night


        when (condition) {
            "Sunny", "Clear","Smoke" -> {
                if (isNight) {
                    binding.root.setBackgroundResource(R.drawable.night_clear_bg)
                    binding.weatherIcon.setImageResource(R.drawable.night_clear)
                } else {
                    binding.root.setBackgroundResource(R.drawable.sunny_bg)
                    binding.weatherIcon.setImageResource(R.drawable.sun)
                }
            }
            "Clouds", "Partly Cloudy", "Overcast", "Mist", "Haze", "Foggy" -> {
                if (isNight) {
                    binding.root.setBackgroundResource(R.drawable.night_clouds_bg)
                    binding.weatherIcon.setImageResource(R.drawable.night_cloud)
                } else {
                    binding.root.setBackgroundResource(R.drawable.clouds_bg)
                    binding.weatherIcon.setImageResource(R.drawable.cloud1)
                }
            }
            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                if (isNight) {
                    binding.root.setBackgroundResource(R.drawable.night_rain_bg)
                    binding.weatherIcon.setImageResource(R.drawable.night_rain)
                } else {
                    binding.root.setBackgroundResource(R.drawable.rain_bg)
                    binding.weatherIcon.setImageResource(R.drawable.rain1)
                }
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                if (isNight) {
                    binding.root.setBackgroundResource(R.drawable.night_snow_bg)
                    binding.weatherIcon.setImageResource(R.drawable.night_snow)
                } else {
                    binding.root.setBackgroundResource(R.drawable.snow_bg)
                    binding.weatherIcon.setImageResource(R.drawable.snow1)
                }
            }
        }


        // 🔹 Change text color dynamically
        val textColor = if (isNight) android.graphics.Color.WHITE else android.graphics.Color.BLACK

        binding.tempText.setTextColor(textColor)
        binding.weatherCondition.setTextColor(textColor)
        binding.minMaxTemp.setTextColor(textColor)
        binding.locationText.setTextColor(textColor)
        binding.dateText.setTextColor(textColor)

    }

    private fun isNightTime(sunrise: Int, sunset: Int): Boolean {
        val currentTime = System.currentTimeMillis() / 1000 // Convert to seconds for comparison
        return currentTime < sunrise || currentTime > sunset
    }


    private fun date(): String {

        val sdf = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
        return sdf.format((Date()))
    }

    private fun fetchAirQuality(cityName: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiInterface::class.java)

        val geoCall = api.getGeoLocation(cityName, "eacc1aee25aacd65c56be05821cc4cce")

        geoCall.enqueue(object : Callback<List<GeoLocation>> {
            override fun onResponse(call: Call<List<GeoLocation>>, response: Response<List<GeoLocation>>) {
                val location = response.body()?.firstOrNull()
                if (location != null) {
                    fetchAirPollution(location.lat, location.lon)
//                    binding.locationText.text = "$cityName".toUpperCase()  // Update UI with city name
                    binding.locationText.text = cityName.uppercase()
                } else {
                    binding.aqiValue.text = "City not found"
                }
            }

            override fun onFailure(call: Call<List<GeoLocation>>, t: Throwable) {
                Log.e("API_ERROR", "Failed to fetch location")
                binding.aqiValue.text = "Error fetching data"
            }
        })
    }


    private fun fetchAirPollution(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ApiInterface::class.java)

        val airPollutionCall = api.getAirQuality(lat, lon, "eacc1aee25aacd65c56be05821cc4cce")

        airPollutionCall.enqueue(object : Callback<AirQuality> {
            override fun onResponse(call: Call<AirQuality>, response: Response<AirQuality>) {
                if (response.isSuccessful) {
                    val airQuality = response.body()
                    val pm25 = airQuality?.list?.firstOrNull()?.components?.pm2_5

                    if (pm25 != null) {
                        val roundedPm25 = pm25.roundToInt() // Round PM2.5 value
                        binding.aqiValue.text = "$roundedPm25"

                        // 🌟 Update UI Based on PM2.5 Level
                        when {
                            roundedPm25 <= 50 -> {
                                binding.aqiCategory.setImageResource(R.drawable.verygoodaqi)
                                binding.aqiEmoji.setImageResource(R.drawable.ic_smiley_happy)
                            }

                            roundedPm25 in 51..100 -> {
                                binding.aqiCategory.setImageResource(R.drawable.goodaqi)
                                binding.aqiEmoji.setImageResource(R.drawable.goodsmileyaqi)
                            }

                            roundedPm25 in 101..150 -> {
                                binding.aqiCategory.setImageResource(R.drawable.fairaqi)
                                binding.aqiEmoji.setImageResource(R.drawable.fairsmileyaqi)
                            }

                            roundedPm25 in 151..200 -> {
                                binding.aqiCategory.setImageResource(R.drawable.pooraqi)
                                binding.aqiEmoji.setImageResource(R.drawable.poorsmileyaqi)
                            }

                            roundedPm25 in 201..250 -> {
                                binding.aqiCategory.setImageResource(R.drawable.verypooraqi)
                                binding.aqiEmoji.setImageResource(R.drawable.verypoorsmileyaqi)
                            }
                        }
                    } else {
                        binding.aqiValue.text = "AQI Data Not Available"
                    }
                } else {
                    Log.e("API_ERROR", "Response failed: ${response.errorBody()?.string()}")
                    binding.aqiValue.text = "API Error"
                }
            }

            override fun onFailure(call: Call<AirQuality>, t: Throwable) {
                Log.e("API_ERROR", "Failed to fetch air pollution data: ${t.message}")
                binding.aqiValue.text = "Network Error"
            }
        })
    }




}



