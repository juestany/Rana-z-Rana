package com.example.sr13.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.sr13.R

class DoctorMyPatientsActivity : AppCompatActivity() {

    private lateinit var addPatientBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_my_patients)

        addPatientBtn = findViewById(R.id.addPatientBtn)

        addPatientBtn.setOnClickListener() {
            val intent = Intent(this@DoctorMyPatientsActivity, DoctorAddPatientActivity::class.java)
            startActivity(intent)
        }
    }

    // TODO: Recycler View with patients from the database
}