package com.example.sr13.doctor

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.bumptech.glide.Glide
import com.example.sr13.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorMainActivity : AppCompatActivity() {

    private lateinit var patientNameMain: TextView
    private lateinit var patientRoleMain: TextView
    private lateinit var patientProfilePicMain: ImageFilterView
    private lateinit var patientSubmitReportBtn: Button
    private lateinit var patientSubmittedReportsRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_main)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        patientNameMain = findViewById(R.id.patientNameMain)
        patientRoleMain = findViewById(R.id.patientRoleMain)
        patientProfilePicMain = findViewById(R.id.patientProfilePicMain)
        patientSubmitReportBtn = findViewById(R.id.patientSubmitReportBtn)
        patientSubmittedReportsRecyclerView = findViewById(R.id.patientSubmittedReportsRecyclerView)

        // Set up initial values or listeners here
        getDoctorData()

        // Set a click listener for the button
        patientSubmitReportBtn.setOnClickListener {
            // Handle button click
            // For example, start a new activity or show a message
        }

        // Set up RecyclerView adapter, layout manager, etc.
        // patientSubmittedReportsRecyclerView.layoutManager = LinearLayoutManager(this)
        // patientSubmittedReportsRecyclerView.adapter = YourAdapter()
    }

    private fun getDoctorData() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = auth.currentUser?.uid

        userId?.let { uid ->
            firestore.collection("doctor")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        val title = document.getString("title")
                        val address = document.getString("adress") // Note: fixed typo to "address"
                        val phoneNumber = document.getString("phoneNumber")
                        val imageUrl = document.getString("imageId")

                        patientNameMain.text = "$firstName $lastName"
                        patientRoleMain.text = title

                        Log.d("DoctorData", "Fetched imageUrl: $imageUrl")

                        // Fetch and display the image from Firebase Storage using URL
                        imageUrl?.let {
                            fetchImageFromFirebaseStorage(it)
                        }
                    } else {
                        Log.e("DoctorData", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DoctorData", "Error fetching doctor data", exception)
                }
        }
    }

    private fun fetchImageFromFirebaseStorage(imageUrl: String) {
        // Load the image into ImageFilterView using Glide
        Glide.with(this)
            .load(imageUrl)
            .into(patientProfilePicMain)
    }
}
