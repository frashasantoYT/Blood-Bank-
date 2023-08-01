package com.kaboom.bloodbank

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.kaboom.bloodbank.db.DonorDB
import com.kaboom.bloodbank.db.Users
import com.kaboom.bloodbank.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var registerBinding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(registerBinding.root)

        auth = Firebase.auth
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.title = "Register"
        }

        setupBloodGroupSpinner()

        registerBinding.btnRegister.setOnClickListener {
            val email = registerBinding.etEmail.text.toString()
            val password = registerBinding.etPassword.text.toString()
            val fullName = registerBinding.etFullName.text.toString()
            val address = registerBinding.etAddress.text.toString()
            val bloodGroup = registerBinding.spinnerBloodGroup.selectedItem.toString()
            val dateOfBirth = registerBinding.etDOB.text.toString()
            val phoneNumber = registerBinding.etPhoneNo.text.toString()
            val isBecomeDonor = registerBinding.chkBecomeDonor.isChecked

            // Perform email and password authentication, then save user data
            signUpWithFirebase(email, password, fullName, address, bloodGroup, dateOfBirth, phoneNumber, isBecomeDonor)
        }
    }



    private fun signUpWithFirebase(
        email: String,
        password: String,
        fullName: String,
        address: String,
        bloodGroup: String,
        dateOfBirth: String,
        phoneNumber: String,
        isBecomeDonor: Boolean,
    ) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email format
            Toast.makeText(applicationContext, "Invalid email address", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the password meets the required criteria (e.g., minimum length)
        if (password.length < 6) {
            Toast.makeText(applicationContext, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if any of the fields are empty
        if (fullName.isEmpty() || address.isEmpty() || dateOfBirth.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(applicationContext, "Please fill in all the required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val confirmPassword = registerBinding.etConfirmPassword.text.toString()
        if (password != confirmPassword) {
            Toast.makeText(
                applicationContext,
                "Password and Confirm Password do not match",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        registerBinding.btnRegister.isClickable = false
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // User registration successful, get the user ID
                val userId = auth.currentUser?.uid

                if (userId != null) {
                    val databaseReference = FirebaseDatabase.getInstance().reference
                    val user = Users(address, bloodGroup, dateOfBirth, email, fullName, phoneNumber)
                    val donor = DonorDB(address, bloodGroup, dateOfBirth, email, fullName, phoneNumber)

                    if (isBecomeDonor) {
                        // Save user details to the "Donors" database
                        databaseReference.child("donors").child(userId).setValue(donor)
                            .addOnSuccessListener {
                                Toast.makeText(applicationContext, "Your account has been created as a donor", Toast.LENGTH_SHORT).show()
                                finish()
                                registerBinding.btnRegister.isClickable = true
                            }
                            .addOnFailureListener {
                                Toast.makeText(applicationContext, "Failed to save donor data", Toast.LENGTH_SHORT).show()
                                finish()
                                registerBinding.btnRegister.isClickable = true
                            }
                    } else {
                        // Save user details to the "users" database
                        databaseReference.child("users").child(userId).setValue(user)
                            .addOnSuccessListener {
                                Toast.makeText(applicationContext, "Your account has been created", Toast.LENGTH_SHORT).show()
                                finish()
                                registerBinding.btnRegister.isClickable = true
                            }
                            .addOnFailureListener {
                                Toast.makeText(applicationContext, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                finish()
                                registerBinding.btnRegister.isClickable = true
                            }
                    }
                }
            } else {
                // User registration failed
                Toast.makeText(applicationContext, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                registerBinding.btnRegister.isClickable = true
            }
        }
    }

    private fun setupBloodGroupSpinner() {
        val spinnerBloodGroup: Spinner = registerBinding.spinnerBloodGroup
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
}

