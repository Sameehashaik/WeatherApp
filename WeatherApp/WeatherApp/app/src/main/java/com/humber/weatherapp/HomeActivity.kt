package com.humber.weatherapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : ComponentActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var locationName: TextView
    private lateinit var tempCard: CardView
    private lateinit var feelsLikeCard: CardView
    private lateinit var windSpeedCard: CardView
    private lateinit var descriptionCard: CardView
    private lateinit var humidityCard: CardView
    private lateinit var pressureCard: CardView
    private lateinit var visibilityCard: CardView
    private lateinit var aqiCard: CardView
    private lateinit var uvIndexCard: CardView
    private lateinit var sunriseTimeCard: CardView
    private lateinit var sunsetTimeCard: CardView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val apiKey = "f2f9f4228331b45522cce65dce2b8144"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize the UI components
        bottomNav = findViewById(R.id.bottomNav)
        locationName = findViewById(R.id.locationName)
        tempCard = findViewById(R.id.tempCard)
        feelsLikeCard = findViewById(R.id.feelsLikeCard)
        windSpeedCard = findViewById(R.id.windSpeedCard)
        descriptionCard = findViewById(R.id.descriptionCard)
        humidityCard = findViewById(R.id.humidityCard)
        pressureCard = findViewById(R.id.pressureCard)
        visibilityCard = findViewById(R.id.visibilityCard)
        aqiCard = findViewById(R.id.aqiCard)
        uvIndexCard = findViewById(R.id.uvCard)
        sunriseTimeCard = findViewById(R.id.sunriseCard)
        sunsetTimeCard = findViewById(R.id.sunsetCard)

        loadUserPreferences()

        // Bottom Navigation click listener
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_locations -> {
                    startActivity(Intent(this, PreferencesActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserPreferences() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("WeatherAppData").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val city = document.getString("location") ?: "Unknown"
                    val preferences = document.get("preferences") as? Map<String, Boolean> ?: emptyMap()
                    fetchWeatherData(city, preferences)
                } else {
                    Toast.makeText(this, "No preferences found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load preferences", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchWeatherData(city: String, preferences: Map<String, Boolean>) {
        val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
        val client = OkHttpClient()
        val weatherRequest = Request.Builder().url(weatherUrl).build()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get weather data
                val weatherResponse = client.newCall(weatherRequest).execute()
                val weatherResponseData = weatherResponse.body?.string()

                if (weatherResponseData != null) {
                    Log.d("WeatherData", "API Response: $weatherResponseData")  // Log the response

                    val json = JSONObject(weatherResponseData)
                    val lat = json.getJSONObject("coord").getDouble("lat")
                    val lon = json.getJSONObject("coord").getDouble("lon")

                    val weatherJson = JSONObject(weatherResponseData)
                    updateUI(preferences, weatherJson, city)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeActivity, "Error: Empty response from API", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Error fetching weather data", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }


    private fun updateUI(preferences: Map<String, Boolean>, weatherJson: JSONObject, city: String) {
        runOnUiThread {
            locationName.text = "$city" // Update location name

            // Temperature
            if (preferences["Temperature"] == true) {
                val temp = weatherJson.getJSONObject("main").getDouble("temp")
                tempCard.findViewById<TextView>(R.id.tempText).text = "🌡 Temperature: $temp°C"
            } else {
                tempCard.visibility = CardView.GONE
            }

            // Feels Like
            if (preferences["Feels Like"] == true) {
                val feelsLike = weatherJson.getJSONObject("main").getDouble("feels_like")
                feelsLikeCard.findViewById<TextView>(R.id.feelsLikeText).text = "🤔 Feels Like: $feelsLike°C"
            } else {
                feelsLikeCard.visibility = CardView.GONE
            }

            // Wind Speed
            if (preferences["Windspeed"] == true) {
                val windSpeed = weatherJson.getJSONObject("wind").getDouble("speed")
                windSpeedCard.findViewById<TextView>(R.id.windSpeedText).text = "💨 Wind Speed: $windSpeed m/s"
            } else {
                windSpeedCard.visibility = CardView.GONE
            }

            // Weather Description
            if (preferences["Weather Description"] == true) {
                val description = weatherJson.getJSONArray("weather").getJSONObject(0).getString("description")
                descriptionCard.findViewById<TextView>(R.id.descriptionText).text = "🌥 Description: $description"
            } else {
                descriptionCard.visibility = CardView.GONE
            }

            // Humidity
            if (preferences["Humidity"] == true) {
                val humidity = weatherJson.getJSONObject("main").getInt("humidity")
                humidityCard.findViewById<TextView>(R.id.humidityText).text = "💧 Humidity: $humidity%"
            } else {
                humidityCard.visibility = CardView.GONE
            }

            // Pressure
            if (preferences["Pressure"] == true) {
                val pressure = weatherJson.getJSONObject("main").getInt("pressure")
                pressureCard.findViewById<TextView>(R.id.pressureText).text = "⚙️ Pressure: $pressure hPa"
            } else {
                pressureCard.visibility = CardView.GONE
            }

            // Visibility
            if (preferences["Visibility"] == true) {
                val visibility = weatherJson.getInt("visibility") / 1000 // Convert to km
                visibilityCard.findViewById<TextView>(R.id.visibilityText).text = "👀 Visibility: $visibility km"
            } else {
                visibilityCard.visibility = CardView.GONE
            }

            // AQI (Air Quality Index)
            if (preferences["Air Quality Index (AQI)"] == true) {
                // Fetch AQI data if necessary or use a placeholder
                aqiCard.findViewById<TextView>(R.id.aqiText).text = "🌫 AQI: 50" // Placeholder value
            } else {
                aqiCard.visibility = CardView.GONE
            }

            // UV Index
            if (preferences["UV Index"] == true) {
                // Fetch UV index data if necessary or use a placeholder
                uvIndexCard.findViewById<TextView>(R.id.uvText).text = "☀️ UV Index: 3" // Placeholder value
            } else {
                uvIndexCard.visibility = CardView.GONE
            }

            // Sunrise Time
            if (preferences["Sunrise Time"] == true) {
                val sunrise = weatherJson.getJSONObject("sys").getLong("sunrise")
                sunriseTimeCard.findViewById<TextView>(R.id.sunriseText).text = "🌅 Sunrise: ${formatTime(sunrise)}"
            } else {
                sunriseTimeCard.visibility = CardView.GONE
            }

            // Sunset Time
            if (preferences["Sunset Time"] == true) {
                val sunset = weatherJson.getJSONObject("sys").getLong("sunset")
                sunsetTimeCard.findViewById<TextView>(R.id.sunsetText).text = "🌇 Sunset: ${formatTime(sunset)}"
            } else {
                sunsetTimeCard.visibility = CardView.GONE
            }
        }
    }

    // Helper function to format Unix time (seconds) to human-readable time
    private fun formatTime(timeInSeconds: Long): String {
        val date = java.util.Date(timeInSeconds * 1000)
        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return format.format(date)
    }
}
