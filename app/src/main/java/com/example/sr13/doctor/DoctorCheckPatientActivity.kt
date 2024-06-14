package com.example.sr13.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sr13.R

class DoctorCheckPatientActivity : AppCompatActivity() {

    private lateinit var removePatientBtn: Button

    // TODO: update name, profile pic etc through patient ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_check_patient)

        val patientName = intent.getStringExtra("PATIENT_NAME")
        val patientNameMain: TextView = findViewById(R.id.patientNameMain)
        patientNameMain.text = patientName

        removePatientBtn = findViewById(R.id.removePatientBtn)
        removePatientBtn.setOnClickListener {
            // TODO: remove patient
            val intent = Intent(this@DoctorCheckPatientActivity, DoctorMyPatientsActivity::class.java)
            startActivity(intent)
        }
    }
}
