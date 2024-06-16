package com.example.sr13.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sr13.R

class DoctorAddPatientSuccessActivity : AppCompatActivity() {

    private lateinit var textViewNewPatientLogin: TextView
    private lateinit var textViewNewPatientPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_login_data_for_patient)

        textViewNewPatientLogin = findViewById(R.id.newPatientLogin)
        textViewNewPatientPassword = findViewById(R.id.newPatientPassword)

        val intent = intent
        val patientLogin = intent.getStringExtra("patientLogin")
        val patientPassword = intent.getStringExtra("patientPassword")
        textViewNewPatientLogin.text = patientLogin
        textViewNewPatientPassword.text = patientPassword
    }

    fun onBackToPatientsClicked(view: android.view.View) {
        val intent = Intent(this, DoctorMyPatientsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
