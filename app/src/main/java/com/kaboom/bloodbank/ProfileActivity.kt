package com.kaboom.bloodbank
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.recreate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kaboom.bloodbank.databinding.ActivityProfileBinding
import com.kaboom.bloodbank.db.DonorDB
import com.kaboom.bloodbank.db.Users

class ProfileActivity : AppCompatActivity() {
    private lateinit var bindingProfile: ActivityProfileBinding
    private lateinit var currentUser: FirebaseUser
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val myUsersReference: DatabaseReference = database.reference.child("users")
    private val myDonorsReference: DatabaseReference = database.reference.child("donors")
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingProfile = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bindingProfile.root)
        supportActionBar?.title = "Profile"
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Auth
        val auth: FirebaseAuth = FirebaseAuth.getInstance()

        // Get the current user
        currentUser = auth.currentUser!!

        // User is logged in, retrieve data and populate the UI'
        clearEditTextFields()

        retrieveDataFromDatabase()

        bindingProfile.btnProfile.setOnClickListener {
            val email = bindingProfile.etEmail.text.toString()
            val fullName = bindingProfile.etFullName.text.toString()
            val bloodGroup = bindingProfile.etBloodGroup.text.toString()
            val address = bindingProfile.etAddress.text.toString()
            val dateOfBirth = bindingProfile.etDOB.text.toString()
            val phoneNumber = bindingProfile.etPhoneNo.text.toString()

            updateUserData(email, fullName, bloodGroup, address, dateOfBirth, phoneNumber)

            // Refresh the activity to reflect the updated data
            recreate()
        }
    }



    private fun displayUserData(
        email: String,
        fullName: String,
        bloodGroup: String,
        address: String,
        dateOfBirth: String,
        phoneNumber: String
    ) {
        bindingProfile.tvFullName.text = fullName
        bindingProfile.etFullName.setText(fullName)
        bindingProfile.etBloodGroup.setText(bloodGroup)
        bindingProfile.etAddress.setText(address)
        bindingProfile.etDOB.setText(dateOfBirth)
        bindingProfile.etPhoneNo.setText(phoneNumber)
        bindingProfile.etEmail.setText(email)
    }



    private fun updateUserData(
        email: String,
        fullName: String,
        bloodGroup: String,
        address: String,
        dateOfBirth: String,
        phoneNumber: String,
    ) {
        bindingProfile.btnProfile.isClickable = false

        // Check if the current user is a donor or a regular user
        isCurrentUserDonor { isDonor ->

            val currentUserReference: DatabaseReference = if(isDonor) {
                myDonorsReference.child(currentUser.uid)
            } else {
                myUsersReference.child(currentUser.uid)
            }

            val userData = hashMapOf(
                "email" to email,
                "fullName" to fullName,
                "bloodGroup" to bloodGroup,
                "address" to address,
                "dateOfBirth" to dateOfBirth,
                "phoneNumber" to phoneNumber
            )

            currentUserReference.updateChildren(userData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Your data has been updated Successfully", Toast.LENGTH_SHORT).show()
                    bindingProfile.btnProfile.isClickable = true
                }
                .addOnFailureListener {
                    Toast.makeText(applicationContext, "Failed to save user data", Toast.LENGTH_SHORT).show()
                    bindingProfile.btnProfile.isClickable = true
                }
        }
    }

    private fun isCurrentUserDonor(callback: (Boolean) -> Unit) {
        // Retrieve the current user's data from the "donors" node
        val currentUserId = currentUser.uid
        myDonorsReference.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val donor = snapshot.getValue(DonorDB::class.java)
                callback(donor != null) // If donor is not null, the current user is a donor, otherwise a regular user
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
                callback(false) // On error, consider the current user as a regular user
            }
        })
    }



    private fun clearEditTextFields() {
        bindingProfile.etFullName.setText("")
        bindingProfile.etBloodGroup.setText("")
        bindingProfile.etAddress.setText("")
        bindingProfile.etDOB.setText("")
        bindingProfile.etPhoneNo.setText("")
        bindingProfile.etEmail.setText("")
        bindingProfile.tvStatus.text = ""
        bindingProfile.tvFullName.text = ""

    }




    // Function to update the UI based on the user type (donor or regular user)
    private fun updateUIForUserType(isDonor: Boolean, user: Users?, donor: DonorDB?) {
        if (isDonor) {
            // The user is a donor, so display the donor's data
            if (donor != null) {
                displayUserData(donor.email, donor.fullName, donor.bloodGroup, donor.address, donor.dateOfBirth, donor.phoneNumber)
                bindingProfile.tvStatus.text = "Registered as a Donor"
            } else {
                // Handle the case when donor data is not available
                Toast.makeText(this@ProfileActivity, "Donor data not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            // The user is a regular user, so display the user's data
            if (user != null) {
                displayUserData(user.email, user.fullName, user.bloodGroup, user.address, user.dateOfBirth, user.phoneNumber)
                bindingProfile.tvStatus.text = "Not registered as a donor"
            } else {
                // Handle the case when user data is not available
                Toast.makeText(this@ProfileActivity, "User data not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retrieveDataFromDatabase() {
        val currentUserId = currentUser.uid

        // Check if the user exists in the "users" database
        myUsersReference.child(currentUserId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)

                // If the user exists in the "users" database, update the UI
                if (user != null) {
                    updateUIForUserType(false, user, null)
                } else {
                    // If the user doesn't exist in the "users" database,
                    // check if the user exists in the "donors" database
                    myDonorsReference.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val donor = snapshot.getValue(DonorDB::class.java)

                            // If the user exists in the "donors" database, update the UI
                            if (donor != null) {
                                updateUIForUserType(true, null, donor)
                            } else {
                                // If the user doesn't exist in either database
                                Toast.makeText(this@ProfileActivity, "User data not available", Toast.LENGTH_SHORT).show()
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





    private fun retrieveDataFromDatabasePreviousVersion() {
        val currentUserId = currentUser.uid

        // Check if the user exists in the "users" database
        myUsersReference.child(currentUserId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)

                if (user != null) {
                    // The user exists in the "users" database
                    displayUserData(user.email, user.fullName, user.bloodGroup, user.address, user.dateOfBirth, user.phoneNumber)
                    bindingProfile.tvStatus.text = "Not registered as a donor"
                } else {
                    // If the user doesn't exist in the "users" database,
                    // check if the user exists in the "donors" database
                    myDonorsReference.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val donor = snapshot.getValue(DonorDB::class.java)

                            if (donor != null) {
                                // The user exists in the "donors" database
                                val donorUser = Users(
                                    email = donor.email,
                                    fullName = donor.fullName,
                                    bloodGroup = donor.bloodGroup,
                                    address = donor.address,
                                    dateOfBirth = donor.dateOfBirth,
                                    phoneNumber = donor.phoneNumber,
                                    isDonor = true // Set isDonor to true for donors
                                )
                                displayUserData(donorUser.email, donorUser.fullName, donorUser.bloodGroup, donorUser.address, donorUser.dateOfBirth, donorUser.phoneNumber)
                                bindingProfile.tvStatus.text = "Registered as a Donor"
                            } else {
                                // The user doesn't exist in either database
                                Toast.makeText(this@ProfileActivity, "User data not available", Toast.LENGTH_SHORT).show()
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
