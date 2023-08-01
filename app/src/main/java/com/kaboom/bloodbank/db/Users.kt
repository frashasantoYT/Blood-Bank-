package com.kaboom.bloodbank.db

data class Users(
    val address: String = "",
    val bloodGroup: String  = "",
    val dateOfBirth: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val isDonor: Boolean = false
)
