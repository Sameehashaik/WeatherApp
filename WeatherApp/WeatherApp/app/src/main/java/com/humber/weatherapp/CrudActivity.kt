package com.humber.weatherapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.humber.weatherapp.databinding.ActivityCrudBinding

class CrudActivity : AppCompatActivity() {

    private lateinit var crudBinding: ActivityCrudBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crudBinding = ActivityCrudBinding.inflate(layoutInflater)
        setContentView(crudBinding.root)

        // Get the Firebase Authentication instance
        val auth = FirebaseAuth.getInstance()

// Get the Firebase Realtime Database instance
        val database = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid // This is the user's unique ID

            val usersCollection = database.collection("users") // Reference to the "users" collection
            val userDocument = usersCollection.document(userId) // Reference to the user's specific document

            // save a location to db that a user would prefer
            crudBinding.saveCityBtn.setOnClickListener{
                val userLocation = crudBinding.saveCityEt.text.toString()

                if (userLocation.isEmpty()){
                    Toast.makeText(this, "Please enter a city", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val userData = hashMapOf(
                    "email" to currentUser.email,
                    "location" to userLocation,
                    "userid" to userId,
                    "username" to currentUser.displayName
                    )
                userDocument.set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Location saved to db", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "${error.message}", Toast.LENGTH_LONG).show()
                    }
            }

            // Fetching user's location from the database
            crudBinding.fetchCityBtn.setOnClickListener{
                userDocument.get()
                    .addOnSuccessListener { document ->
                        if(document.exists()){
                            val userData = document.data
                            val savedLocation = userData?.get("location")
                            crudBinding.userCityTv.text = savedLocation.toString().trim()
                        }
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "Error retrieving document: ${error.message}", Toast.LENGTH_LONG).show()
                    }
            }

            crudBinding.updateCityBtn.setOnClickListener{
                val updateLocation = crudBinding.updateUserCityEt.text.toString().trim()

                if (updateLocation.isEmpty()){
                    Toast.makeText(this, "Please enter a city", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                userDocument.update("location", updateLocation)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Location updated successfully", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "${error.message}", Toast.LENGTH_LONG).show()
                    }
                // We can use hashmap like above for multiple updates at once
            }

            crudBinding.deleteCityBtn.setOnClickListener{

//                 To delete the entire data of the user... cool but use caution
//                userDocument.delete()
//                    .addOnSuccessListener {
//                        Toast.makeText(this, "Deleted successfully", Toast.LENGTH_LONG).show()
//                    }
//                    .addOnFailureListener { error ->
//                        Toast.makeText(this, "${error.message}", Toast.LENGTH_LONG).show()
//                    }

                // instead we can delete just one particular data
                // FieldValue class from firebase does this but we use the same update
                // to apply the changes
                userDocument.update("location", FieldValue.delete())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Location deleted successfully", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "${error.message}", Toast.LENGTH_LONG).show()
                    }

            }

        } else {
            // Handle the case where the user is not signed in
            // You might want to disable certain features or redirect to the sign-in screen
        }




    }
}