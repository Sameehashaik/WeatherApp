package com.humber.weatherapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PreferencesActivity : ComponentActivity() {
    private lateinit var cityEditText: EditText
    private lateinit var temperatureCheckbox: CheckBox
    private lateinit var feelsLikeCheckbox: CheckBox
    private lateinit var windspeedCheckbox: CheckBox
    private lateinit var uvCheckbox: CheckBox
    private lateinit var pressureCheckbox: CheckBox
    private lateinit var humidityCheckbox: CheckBox
    private lateinit var visibilityCheckbox: CheckBox
    private lateinit var sunriseCheckbox: CheckBox
    private lateinit var sunsetCheckbox: CheckBox
    private lateinit var aqiCheckbox: CheckBox
    private lateinit var weatherDescCheckbox: CheckBox
    private lateinit var submitBtn: Button
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        cityEditText = findViewById(R.id.cityEditText)
        temperatureCheckbox = findViewById(R.id.temperatureCheckbox)
        feelsLikeCheckbox = findViewById(R.id.feelsLikeCheckbox)
        windspeedCheckbox = findViewById(R.id.windspeedCheckbox)
        uvCheckbox = findViewById(R.id.uvCheckbox)
        pressureCheckbox = findViewById(R.id.pressureCheckbox)
        humidityCheckbox = findViewById(R.id.humidityCheckbox)
        visibilityCheckbox = findViewById(R.id.visibilityCheckbox)
        sunriseCheckbox = findViewById(R.id.sunriseCheckbox)
        sunsetCheckbox = findViewById(R.id.sunsetCheckbox)
        aqiCheckbox = findViewById(R.id.aqiCheckbox)
        weatherDescCheckbox = findViewById(R.id.weatherDescCheckbox)
        submitBtn = findViewById(R.id.submitBtn)

        submitBtn.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val city = cityEditText.text.toString()
            if (city.isEmpty()) {
                Toast.makeText(this, "Please enter your home city", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val preferences = mutableMapOf<String, Boolean>()
            preferences["Temperature"] = temperatureCheckbox.isChecked
            preferences["Feels Like"] = feelsLikeCheckbox.isChecked
            preferences["Windspeed"] = windspeedCheckbox.isChecked
            preferences["UV Index"] = uvCheckbox.isChecked
            preferences["Pressure"] = pressureCheckbox.isChecked
            preferences["Humidity"] = humidityCheckbox.isChecked
            preferences["Visibility"] = visibilityCheckbox.isChecked
            preferences["Sunrise Time"] = sunriseCheckbox.isChecked
            preferences["Sunset Time"] = sunsetCheckbox.isChecked
            preferences["Air Quality Index (AQI)"] = aqiCheckbox.isChecked
            preferences["Weather Description"] = weatherDescCheckbox.isChecked

            val userData = mapOf(
                "userId" to user.uid,
                "email" to user.email,
                "username" to (user.displayName ?: "Unknown"),
                "location" to city,
                "preferences" to preferences
            )

            db.collection("WeatherAppData").document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Preferences saved successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save preferences", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
