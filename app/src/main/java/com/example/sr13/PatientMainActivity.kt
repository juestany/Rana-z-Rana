package com.example.sr13

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView

class PatientMainActivity : AppCompatActivity() {

    private lateinit var patientNameMain: TextView
    private lateinit var patientRoleMain: TextView
    private lateinit var patientProfilePicMain: ImageFilterView
    private lateinit var patientSubmitReportBtn: Button
    private lateinit var patientSubmittedReportsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_main)

        // Initialize views
        patientNameMain = findViewById(R.id.patientNameMain)
        patientRoleMain = findViewById(R.id.patientRoleMain)
        patientProfilePicMain = findViewById(R.id.patientProfilePicMain)
        patientSubmitReportBtn = findViewById(R.id.patientSubmitReportBtn)
        patientSubmittedReportsRecyclerView = findViewById(R.id.patientSubmittedReportsRecyclerView)

        // Set up initial values or listeners here
        // For example, set the text for the patient name and role
        patientNameMain.text = "Leokadia Rafałowicz" // You might set this from a data source
        patientRoleMain.text = "Pacjent" // You might set this from a data source

        // Set a click listener for the button
        patientSubmitReportBtn.setOnClickListener {
            // Handle button click
            // For example, start a new activity or show a message
        }

        // Set up RecyclerView adapter, layout manager, etc.
        // patientSubmittedReportsRecyclerView.layoutManager = LinearLayoutManager(this)
        // patientSubmittedReportsRecyclerView.adapter = YourAdapter()
    }
}
