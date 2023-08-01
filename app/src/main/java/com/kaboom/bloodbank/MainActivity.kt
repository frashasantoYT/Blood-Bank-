package com.kaboom.bloodbank

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kaboom.bloodbank.databinding.ActivityMainBinding
import com.kaboom.bloodbank.db.DonorDB
import com.kaboom.bloodbank.db.Users


class MainActivity : AppCompatActivity(){
    private val REQUEST_REGISTER_DONOR = 1



    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    private val myReference: DatabaseReference = database.reference.child("users")


    val userList = ArrayList<Users>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setupMainActivityUI()
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            handleNavigationItemSelected(item)
        }
        clearImageView()
        clearTextViews()

        retrieveDataFromDatabase()



       mainBinding.btnRegDonor.setOnClickListener {
           val intent = Intent(this@MainActivity, RegisterAsDonor::class.java)
           startActivity(intent)
       }
        mainBinding.btnfindDonors.setOnClickListener {
            val intent = Intent(this@MainActivity, Donors::class.java)
            startActivity(intent)
            finish()
        }




    }

    private fun setupMainActivityUI() {
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = mainBinding.root
        setContentView(view)
    }

    private fun retrieveDataFromDatabase() {
        val currentUser = auth.currentUser
        val currentUserId = currentUser?.uid

        currentUserId?.let { userId ->
            // Check if the user is registered as a donor in the "donors" database
            val donorReference = FirebaseDatabase.getInstance().reference.child("donors")
            donorReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // User is registered as a donor, retrieve donor information
                        val donor = snapshot.getValue(DonorDB::class.java)
                        if (donor != null) {
                            mainBinding.donorStatusImg.setImageResource(R.drawable.donor)
                            mainBinding.donorStatusText.text = "You can Donate"
                            val fullName = donor.fullName
                            val bloodGroup = donor.bloodGroup
                            mainBinding.tvfullName.text = "Welcome $fullName"
                            mainBinding.txtVBloodGroup.text = bloodGroup
                            mainBinding.btnRegDonor.visibility = View.GONE // Show the button
                        }
                    } else {
                        // User is not registered as a donor, check if the user exists in the "users" database
                        val userReference = FirebaseDatabase.getInstance().reference.child("users")
                        userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    // User exists in the "users" database, retrieve user information
                                    val user = snapshot.getValue(Users::class.java)
                                    if (user != null) {
                                        val fullName = user.fullName
                                        val bloodGroup = user.bloodGroup
                                        mainBinding.donorStatusImg.setImageResource(R.drawable.notreg)
                                        mainBinding.donorStatusText.text = "Please Register as a Donor to donate"

                                        mainBinding.tvfullName.text = "Welcome $fullName"
                                        mainBinding.txtVBloodGroup.text = bloodGroup
                                        mainBinding.btnRegDonor.visibility = View.VISIBLE // Show the button
                                    }
                                } else {
                                    // User does not exist in either "donors" or "users" database
                                    mainBinding.donorStatusImg.setImageDrawable(null)
                                    mainBinding.donorStatusText.text = "Please Register to donate"
                                    mainBinding.btnRegDonor.visibility = View.VISIBLE // Show the button
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

            R.id.navChangePass -> {
                startActivity(Intent(this, ChangePassword::class.java))
                return true
            }






        }
        return false
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun clearTextViews() {
        mainBinding.tvfullName.text = ""
        mainBinding.txtVBloodGroup.text = ""
    }

    private fun clearImageView(){
        mainBinding.donorStatusImg.setImageDrawable(null)
    }

    // Inside MainActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_REGISTER_DONOR && resultCode == RESULT_OK) {
            val isRegisteredAsDonor = data?.getBooleanExtra("isRegisteredAsDonor", false)
            if (isRegisteredAsDonor == true) {
                // The user has registered as a donor, update the UI accordingly
                // You can call retrieveDataFromDatabase() here to refresh the UI
                retrieveDataFromDatabase()
            }
        }
    }

}








