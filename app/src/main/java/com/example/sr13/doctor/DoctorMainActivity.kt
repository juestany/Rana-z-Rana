package com.example.sr13.doctor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
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
                        val address = document.getString("address")
                        val phoneNumber = document.getString("phoneNumber")

                        patientNameMain.text = "$firstName $lastName"
                        patientRoleMain.text = title
                        // Set other views with additional doctor data
                        // For example:
                        // patientAddressTextView.text = address
                        // patientPhoneNumberTextView.text = phoneNumber
                    } else {
                        // Document doesn't exist
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failures
                }
        }
    }
}
