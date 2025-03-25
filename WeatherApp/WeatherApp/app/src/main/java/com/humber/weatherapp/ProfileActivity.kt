package com.humber.weatherapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : ComponentActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
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
    private lateinit var bottomNav: BottomNavigationView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileImageView = findViewById(R.id.profile_icon)
        usernameTextView = findViewById(R.id.username)
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
        bottomNav = findViewById(R.id.bottom_navigation)

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Load profile picture using Glide and make it circular
        Glide.with(this)
            .load(user.photoUrl) // Uses the user's profile picture from Firebase
            .placeholder(R.drawable.ic_profile) // Default placeholder
            .error(R.drawable.ic_profile) // Error image
            .transform(CircleCrop()) // Make the image circular
            .override(400, 400)
            .into(profileImageView)


        // Fetch user data from Firestore
        db.collection("WeatherAppData").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    usernameTextView.text = document.getString("username") ?: "Username not found"

                    // Get preferences and pre-select checkboxes
                    val preferences = document.get("preferences") as? Map<String, Boolean> ?: emptyMap()
                    temperatureCheckbox.isChecked = preferences["Temperature"] ?: false
                    feelsLikeCheckbox.isChecked = preferences["Feels Like"] ?: false
                    windspeedCheckbox.isChecked = preferences["Windspeed"] ?: false
                    uvCheckbox.isChecked = preferences["UV Index"] ?: false
                    pressureCheckbox.isChecked = preferences["Pressure"] ?: false
                    humidityCheckbox.isChecked = preferences["Humidity"] ?: false
                    visibilityCheckbox.isChecked = preferences["Visibility"] ?: false
                    sunriseCheckbox.isChecked = preferences["Sunrise Time"] ?: false
                    sunsetCheckbox.isChecked = preferences["Sunset Time"] ?: false
                    aqiCheckbox.isChecked = preferences["Air Quality Index (AQI)"] ?: false
                    weatherDescCheckbox.isChecked = preferences["Weather Description"] ?: false
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }

        submitBtn.setOnClickListener {
            val updatedPreferences = mapOf(
                "Temperature" to temperatureCheckbox.isChecked,
                "Feels Like" to feelsLikeCheckbox.isChecked,
                "Windspeed" to windspeedCheckbox.isChecked,
                "UV Index" to uvCheckbox.isChecked,
                "Pressure" to pressureCheckbox.isChecked,
                "Humidity" to humidityCheckbox.isChecked,
                "Visibility" to visibilityCheckbox.isChecked,
                "Sunrise Time" to sunriseCheckbox.isChecked,
                "Sunset Time" to sunsetCheckbox.isChecked,
                "Air Quality Index (AQI)" to aqiCheckbox.isChecked,
                "Weather Description" to weatherDescCheckbox.isChecked
            )

            db.collection("WeatherAppData").document(user.uid)
                .update("preferences", updatedPreferences)
                .addOnSuccessListener {
                    Toast.makeText(this, "Preferences updated successfully!", Toast.LENGTH_SHORT).show()
                    recreate() // Reload page
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update preferences", Toast.LENGTH_SHORT).show()
                }
        }

        // Bottom Navigation Logic
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_locations -> {
                    startActivity(Intent(this, PreferencesActivity::class.java))
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}
