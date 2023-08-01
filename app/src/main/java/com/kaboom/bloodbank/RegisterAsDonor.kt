package com.kaboom.bloodbank

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kaboom.bloodbank.databinding.ActivityRegisterAsDonorBinding
import com.kaboom.bloodbank.db.DonorDB
import com.kaboom.bloodbank.db.Users

class RegisterAsDonor : AppCompatActivity() {
    private lateinit var asDonorBinding : ActivityRegisterAsDonorBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
            asDonorBinding = ActivityRegisterAsDonorBinding.inflate(layoutInflater)
            val view = asDonorBinding.root
            super.onCreate(savedInstanceState)
             setContentView(view)
        // Initialize FirebaseAuth
            auth = FirebaseAuth.getInstance()


             supportActionBar?.title = "Donor registration"

             retrieveDataFromDatabase()

             setupBloodGroupSpinner()





       asDonorBinding.btnBecomeDonor.setOnClickListener {
            // Check if the checkbox is checked
            if (asDonorBinding.chkBecomeDonor.isChecked) {
                // If the checkbox is checked, proceed with donor registration
                registerAsDonor()
            } else {
                // If the checkbox is not checked, show a message or handle the case as needed
                Toast.makeText(this, "Please agree to become a donor.", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun registerAsDonor() {
        val currentUser = auth.currentUser
        val currentUserId = currentUser?.uid

        // Check if the current user is not null and has a valid UID
        if (currentUserId != null) {
            // Retrieve the user data from the "users" database
            val userReference = FirebaseDatabase.getInstance().reference.child("users").child(currentUserId)
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // User exists in the "users" database, retrieve user information
                        val user = snapshot.getValue(Users::class.java)
                        if (user != null) {
                            // Create a new entry in the "donors" database
                            val databaseReference = FirebaseDatabase.getInstance().reference.child("donors")
                            val donorKey = databaseReference.push().key
                            if (donorKey != null) {
                                val donorInfo = DonorDB(
                                    user.address,
                                    user.bloodGroup,
                                    user.dateOfBirth,
                                    user.email,
                                    user.fullName,
                                    user.phoneNumber
                                )
                                databaseReference.child(donorKey).setValue(donorInfo)
                                    .addOnSuccessListener {
                                        // Registration success
                                        Toast.makeText(
                                            this@RegisterAsDonor,
                                            "You have successfully registered as a Donor",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Delete the user's data from the "users" database
                                        userReference.removeValue()

                                        // Redirect to MainActivity
                                        intent = Intent(this@RegisterAsDonor, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        // Registration failed
                                        Toast.makeText(
                                            this@RegisterAsDonor,
                                            "Registration failed. Please try again.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error if needed
                }
            })
        }
    }


    private fun setupBloodGroupSpinner() {
        val spinnerBloodGroup: Spinner = asDonorBinding.spinnerBloodGroup
        val bloodGroupOptions: Array<String> = resources.getStringArray(R.array.blood_group_options)
        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(
            this, R.layout.custom_spinner_dropdown_item, bloodGroupOptions
        ) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: TextView = super.getDropDownView(position, convertView, parent) as TextView
                view.setPadding(16, 16, 16, 16) // Add padding to each dropdown item
                return view
            }
        }
        spinnerBloodGroup.adapter = adapter
    }


    private fun displayUserData(
        email: String,
        fullName: String,
        address: String,
        dateOfBirth: String,
        phoneNumber: String
    ) {
        asDonorBinding.etFullName.setText(fullName)
        asDonorBinding.etAddress.setText(address)
        asDonorBinding.etDOB.setText(dateOfBirth)
        asDonorBinding.etPhoneNo.setText(phoneNumber)
        asDonorBinding.etEmail.setText(email)
    }

    private fun retrieveDataFromDatabase() {
        val currentUser = auth.currentUser
        val currentUserId = currentUser?.uid

        currentUserId?.let { userId ->
            // Check if the user is registered as a donor in the "donors" database
            val donorReference = FirebaseDatabase.getInstance().reference.child("donors")
            donorReference.child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // User is registered as a donor, retrieve donor information
                        val donor = snapshot.getValue(DonorDB::class.java)
                        if (donor != null) {



                        }
                    } else {
                        // User is not registered as a donor, check if the user exists in the "users" database
                        val userReference = FirebaseDatabase.getInstance().reference.child("users")
                        userReference.child(userId).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    // User exists in the "users" database, retrieve user information
                                    val user = snapshot.getValue(Users::class.java)
                                    if (user != null) {
                                        displayUserData(user.email, user.fullName, user.phoneNumber, user.dateOfBirth, user.address)
                                    }
                                } else {

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle database error if needed
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error if needed
                }
            })
        }
    }
}