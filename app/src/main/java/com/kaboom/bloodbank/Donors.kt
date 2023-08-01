package com.kaboom.bloodbank

import DonorsAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kaboom.bloodbank.databinding.ActivityDonorsBinding
import com.kaboom.bloodbank.db.DonorDB

class Donors : AppCompatActivity() {
    private lateinit var donorBinding: ActivityDonorsBinding
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val myReference: DatabaseReference = database.reference.child("donors")
    private val donorList = ArrayList<DonorDB>()
    private lateinit var donorsAdapter: DonorsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        donorBinding = ActivityDonorsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val view = donorBinding.root
        setContentView(view)

        supportActionBar?.title = "Donors"

        // Initialize the donorsAdapter and assign it to the RecyclerView
        donorsAdapter = DonorsAdapter(this, donorList)
        donorBinding.donorsRecyclerView.layoutManager = LinearLayoutManager(this)
        donorBinding.donorsRecyclerView.adapter = donorsAdapter

        retrieveDataFromDatabase()

        // Set up the SearchView
        donorBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // If the search query is empty or null, display the original donorList
                if (newText.isNullOrEmpty()) {
                    donorsAdapter.setFilteredList(donorList)
                } else {
                    // Filter the donorList based on the search query and display the filtered list
                    val filteredList = donorList.filter { donor ->
                        donor.bloodGroup.contains(newText, ignoreCase = true)
                    }
                    donorsAdapter.setFilteredList(filteredList)
                }
                return true
            }
        })
    }

    private fun retrieveDataFromDatabase() {
        myReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                donorList.clear()
                for (eachUser in snapshot.children) {
                    val donor = eachUser.getValue(DonorDB::class.java)
                    if (donor != null) {
                        donorList.add(donor)
                    }
                }
                donorsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }
}
