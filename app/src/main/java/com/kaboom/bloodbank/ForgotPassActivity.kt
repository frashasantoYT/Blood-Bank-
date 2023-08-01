package com.kaboom.bloodbank

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.kaboom.bloodbank.databinding.ActivityForgotPassBinding

class ForgotPassActivity : AppCompatActivity() {
    private lateinit var bindingForgotPassword: ActivityForgotPassBinding

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        bindingForgotPassword = ActivityForgotPassBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val view = bindingForgotPassword.root
        setContentView(view)
        val email = bindingForgotPassword.etPassword.text.toString()

        bindingForgotPassword.btnForgotPass.setOnClickListener {
            auth.sendPasswordResetEmail(email).addOnCompleteListener {task ->
                if(task.isSuccessful){
                    Toast.makeText(applicationContext, "We sent a password reset email to your account", Toast.LENGTH_SHORT).show()
                    finish()
                }

            }
        }
    }
}