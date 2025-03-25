package com.humber.weatherapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.humber.weatherapp.databinding.ActivityWeatherBinding
import org.json.JSONObject

class WeatherActivity : AppCompatActivity() {

    private lateinit var weatherBinding: ActivityWeatherBinding
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weatherBinding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(weatherBinding.root)

        // must initialize a new request queue object of volley
        requestQueue = Volley.newRequestQueue(this)

        weatherBinding.getWeathenBtn.setOnClickListener{
            val city = weatherBinding.cityEt.text.toString().trim()
            if (city.isNotEmpty()){
                fetchWeather(city)
            } else {
                Toast.makeText(this, "City cannot be blank", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchWeather(city: String) {
        val apiKey = "" // never commit this key
        if (apiKey.isEmpty()){
            Toast.makeText(this, "API key missing in codebase", Toast.LENGTH_LONG).show()
            return
        }
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response -> parseWeatherResponse(response) },
            { error -> weatherBinding.weatherView.text = "Error: ${error.message}" })

        requestQueue.add(jsonObjectRequest)
    }

    private fun parseWeatherResponse(response: JSONObject) {
        try {
            val main = response.getJSONObject("main")
            val temp = main.getDouble("temp")
            val feelsLike = main.optDouble("feels_like") // Use optDouble to avoid exceptions if the field is missing
            val humidity = main.getInt("humidity")
            val pressure = main.getInt("pressure")

            val weatherArray = response.getJSONArray("weather")
            val weatherObject = weatherArray.getJSONObject(0)
            val weatherDescription = weatherObject.getString("description")
            val weatherMain = weatherObject.getString("main")
            val weatherIcon = weatherObject.getString("icon")

            val windObject = response.optJSONObject("wind") // Use optJSONObject for optional fields
            val windSpeed = windObject?.optDouble("speed")
            val windDegrees = windObject?.optInt("deg")
            val windGust = windObject?.optDouble("gust")

            val cloudsObject = response.optJSONObject("clouds")
            val cloudiness = cloudsObject?.optInt("all")

            val rainObject = response.optJSONObject("rain")
            val rain1h = rainObject?.optDouble("1h")
            val rain3h = rainObject?.optDouble("3h")

            val snowObject = response.optJSONObject("snow")
            val snow1h = snowObject?.optDouble("1h")
            val snow3h = snowObject?.optDouble("3h")

            val sysObject = response.getJSONObject("sys")
            val country = sysObject.getString("country")

            val cityName = response.getString("name")
            val timezone = response.getInt("timezone")

            val result = """
            City: $cityName
            Temperature: $temp°C
            Feels Like: $feelsLike°C
            Description: $weatherDescription ($weatherMain, Icon: $weatherIcon)
            Humidity: $humidity%
            Pressure: $pressure hPa
            Wind Speed: $windSpeed m/s
            Wind Direction: $windDegrees degrees
            Wind Gust: $windGust m/s
            Cloudiness: $cloudiness%
            Rain (1h): $rain1h mm
            Rain (3h): $rain3h mm
            Snow (1h): $snow1h mm
            Snow (3h): $snow3h mm
            Country: $country
            Timezone Offset: $timezone seconds
        """.trimIndent()
            weatherBinding.weatherView.text = result

        } catch (e: Exception) {
            weatherBinding.weatherView.text = "Error parsing data: ${e.message}"
            e.printStackTrace()
            Log.d("error", e.printStackTrace().toString())
        }
    }
}