package com.example.sr13.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sr13.R
import com.example.sr13.firestore.Patient
import com.google.firebase.firestore.FirebaseFirestore

class DoctorCheckPatientActivity : AppCompatActivity() {

    private lateinit var removePatientBtn: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var patientNameMain: TextView
    private lateinit var patientProfilePic: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_check_patient)

        patientNameMain = findViewById(R.id.patientNameMain)
        patientProfilePic = findViewById(R.id.patientProfilePicMain)
        removePatientBtn = findViewById(R.id.removePatientBtn)
        firestore = FirebaseFirestore.getInstance()

        val patientId = intent.getStringExtra("PATIENT_ID")
        if (patientId != null) {
            loadPatientData(patientId)
        }

        removePatientBtn.setOnClickListener {
            val intent = Intent(this@DoctorCheckPatientActivity, DoctorMyPatientsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadPatientData(patientId: String) {
        firestore.collection("patient")
            .document(patientId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val patient = document.toObject(Patient::class.java)
                    val imageUrl = document.getString("imageId")
                    if (patient != null) {
                        val fullName = "${patient.firstName} ${patient.lastName}"
                        patientNameMain.text = fullName
                        imageUrl?.let {
                            fetchImageFromFirebaseStorage(it)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors
            }
    }

    private fun fetchImageFromFirebaseStorage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(patientProfilePic)
    }
}
