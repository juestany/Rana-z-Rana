package com.example.sr13.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sr13.R

class DoctorAddPatientActivity : AppCompatActivity() {

    private lateinit var addPatientName: TextView
    private lateinit var addPatientLastName: TextView
    private lateinit var addPatientPESEL: TextView
    private lateinit var addPatientEmail: TextView
    private lateinit var addPatientNumber: TextView
    private lateinit var addPatientAddress: TextView
    private lateinit var addPatientDate: TextView // TODO: fix this to be a date view
    private lateinit var addPatientBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_add_patient)

        addPatientBtn = findViewById(R.id.addPatientBtn)
        addPatientName = findViewById(R.id.addPatientName)
        addPatientLastName = findViewById(R.id.addPatientLastName)
        addPatientPESEL = findViewById(R.id.addPatientPESEL)
        addPatientEmail = findViewById(R.id.addPatientEmail)
        addPatientNumber = findViewById(R.id.addPatientNumber)
        addPatientAddress = findViewById(R.id.addPatientAddress)
        addPatientDate = findViewById(R.id.addPatientDate)

        addPatientBtn.setOnClickListener() {
            // TODO: add patient to database
            val intent = Intent(this@DoctorAddPatientActivity, DoctorMyPatientsActivity::class.java)
            startActivity(intent)
        }
    }

    // TODO: Recycler View with patients from the database
}