package com.example.sr13

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PatientMainActivity : AppCompatActivity() {

    private lateinit var patientNameMain: TextView
    private lateinit var patientRoleMain: TextView
    private lateinit var patientProfilePicMain: ImageFilterView
    private lateinit var patientSubmitReportBtn: Button
    private lateinit var patientSubmittedReportsRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_main)

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
        getPatientData()

        // Set a click listener for the button
        patientSubmitReportBtn.setOnClickListener {
            // Handle button click
            // For example, start a new activity or show a message
        }

        // Set up RecyclerView adapter, layout manager, etc.
        // patientSubmittedReportsRecyclerView.layoutManager = LinearLayoutManager(this)
        // patientSubmittedReportsRecyclerView.adapter = YourAdapter()
    }

    private fun getPatientData() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = auth.currentUser?.uid

        userId?.let { uid ->
            firestore.collection("patient")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        val phoneNumber = document.getString("phoneNumber")

                        patientNameMain.text = "$firstName $lastName"
                        patientRoleMain.text = "Pacjent"
                        // Set other views with additional patient data
                        // For example:
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
