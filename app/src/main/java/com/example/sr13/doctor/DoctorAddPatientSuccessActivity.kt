package com.example.sr13.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sr13.R

/**
 * Activity to display the login credentials for a newly added patient.
 * This screen is shown after a patient is successfully added.
 */
class DoctorAddPatientSuccessActivity : AppCompatActivity() {

    // UI elements to display the new patient's login and password
    private lateinit var textViewNewPatientLogin: TextView
    private lateinit var textViewNewPatientPassword: TextView

    /**
     * Called when the activity is first created.
     * Initializes the UI components and sets the patient credentials.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_login_data_for_patient)

        // Initialize UI components
        textViewNewPatientLogin = findViewById(R.id.newPatientLogin)
        textViewNewPatientPassword = findViewById(R.id.newPatientPassword)

        // Retrieve patient login credentials from the intent
        val intent = intent
        val patientLogin = intent.getStringExtra("patientLogin") // Get patient login
        val patientPassword = intent.getStringExtra("patientPassword") // Get patient password

        // Set the credentials to the respective TextViews
        textViewNewPatientLogin.text = patientLogin
        textViewNewPatientPassword.text = patientPassword
    }

    /**
     * Handles the event when the "Back to Patients" button is clicked.
     * Navigates to the Doctor's "My Patients" screen.
     */
    fun onBackToPatientsClicked(view: android.view.View) {
        // Intent to navigate back to the "My Patients" screen
        val intent = Intent(this@DoctorAddPatientSuccessActivity, DoctorMyPatientsActivity::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }
}