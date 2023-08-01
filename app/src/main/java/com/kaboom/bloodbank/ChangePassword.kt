package com.kaboom.bloodbank

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.kaboom.bloodbank.databinding.ActivityChangePasswordBinding

class ChangePassword : AppCompatActivity() {
    private lateinit var changePassBinding : ActivityChangePasswordBinding
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        changePassBinding = ActivityChangePasswordBinding.inflate(layoutInflater)
        val view = changePassBinding.root
        super.onCreate(savedInstanceState)
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        changePassBinding.btnLogin.setOnClickListener {
            val newPassword = changePassBinding.etEmail.text.toString().trim()
            val confirmPassword = changePassBinding.etPassword.text.toString().trim()

            // Validate new password and confirm password fields
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please enter both new password and confirm password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update the user's password
            val currentUser = auth.currentUser
            currentUser?.updatePassword(newPassword)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Password updated successfully
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        // Failed to update password
                        Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    }
