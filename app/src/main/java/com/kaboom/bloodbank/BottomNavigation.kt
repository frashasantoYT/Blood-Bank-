package com.kaboom.bloodbank

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.kaboom.bloodbank.databinding.ActivityBottomNavigationBinding
import com.kaboom.bloodbank.databinding.ActivityDonorsBinding

open class BottomNavigation : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var bottomNavBinding: ActivityBottomNavigationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        bottomNavBinding = ActivityBottomNavigationBinding.inflate(layoutInflater)
        val view = bottomNavBinding.root
        super.onCreate(savedInstanceState)
        setContentView(view)

        auth = FirebaseAuth.getInstance()



    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navHome -> {
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }
            R.id.navProfile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }
            R.id.navDonors-> {
                startActivity(Intent(this, Donors::class.java))
                return true
            }
            R.id.navLogout -> {
                logout()
                return true
            }





        }
        return false
    }
}