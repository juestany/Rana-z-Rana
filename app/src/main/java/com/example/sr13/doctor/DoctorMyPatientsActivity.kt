package com.example.sr13.doctor

import com.example.sr13.doctor.my_patients_recyclerview.MyPatientsAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sr13.doctor.my_patients_recyclerview.MyPatientsViewModel
import com.example.sr13.R
import com.example.sr13.firestore.Doctor
import com.example.sr13.firestore.Patient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class DoctorMyPatientsActivity : AppCompatActivity() {

    private lateinit var addPatientBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserId: String

    // TODO: make it so clicking the item shows the patient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_my_patients)

        addPatientBtn = findViewById(R.id.addPatientBtn)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        val recyclerview = findViewById<RecyclerView>(R.id.myPatientsRecyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)

        loadPatientsData()

        addPatientBtn.setOnClickListener {
            val intent = Intent(this@DoctorMyPatientsActivity, DoctorAddPatientActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadPatientsData() {
        // Fetch patient IDs connected to the current doctor (currentUserId)
        firestore.collection("doctor")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val doctorData = document.toObject(Doctor::class.java)
                    val patientIds = doctorData?.patientIds ?: emptyList()

                    // Fetch patient details (names and last names) based on patientIds
                    fetchPatientDetails(patientIds)
                } else {
                    // Handle case where document for current doctor ID doesn't exist
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors
            }
    }

    private fun fetchPatientDetails(patientIds: List<String>) {
        // Initialize an empty list to hold patient view models
        val data = ArrayList<MyPatientsViewModel>()

        // Iterate through patientIds to fetch patient details
        for (patientId in patientIds) {
            firestore.collection("patient")
                .document(patientId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val patient = document.toObject(Patient::class.java)
                        // Add patient name and last name to data list
                        if (patient != null) {
                            val fullName = "${patient.firstName} ${patient.lastName}"
                            data.add(MyPatientsViewModel(R.drawable.ic_profile_icon, fullName))
                            // Notify adapter when data changes
                            val recyclerview = findViewById<RecyclerView>(R.id.myPatientsRecyclerView)
                            recyclerview.adapter?.notifyDataSetChanged()
                        }
                    } else {
                        // Handle case where patient document doesn't exist
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                }
        }

        // Initialize RecyclerView adapter with fetched data
        val adapter = MyPatientsAdapter(data) { patientFullName ->
            // On item click, start DoctorCheckPatientActivity and pass the patient full name
            val intent = Intent(this, DoctorCheckPatientActivity::class.java)
            intent.putExtra("PATIENT_FULL_NAME", patientFullName)
            startActivity(intent)
        }

        val recyclerview = findViewById<RecyclerView>(R.id.myPatientsRecyclerView)
        recyclerview.adapter = adapter
    }
}
