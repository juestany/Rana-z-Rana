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

        addPatientBtn.setOnClickListener() {
            val intent = Intent(this@DoctorAddPatientActivity, DoctorMyPatientsActivity::class.java)
            startActivity(intent)
        }
    }

    // TODO: Recycler View with patients from the database
}