package com.kaboom.bloodbank

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kaboom.bloodbank.databinding.ActivityDrawerBaseBinding
import com.kaboom.bloodbank.databinding.ActivityUpdateProfileBinding
import com.kaboom.bloodbank.db.DonorDB

class UpdateProfile : DrawerBaseActivity(){
    private lateinit var updateProfile: ActivityUpdateProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateProfile = ActivityUpdateProfileBinding.inflate(layoutInflater)
        val view = updateProfile.root
        setContentView(view)

        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            actionBar.title = "Update Profile"
        }




    }

}