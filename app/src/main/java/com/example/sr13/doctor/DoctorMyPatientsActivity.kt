package com.example.sr13.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sr13.R
import com.example.sr13.doctor.my_patients_rv.MyPatientsAdapter
import com.example.sr13.doctor.my_patients_rv.MyPatientsViewModel
import com.example.sr13.firestore.Doctor
import com.example.sr13.firestore.Patient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorMyPatientsActivity : AppCompatActivity() {

    private lateinit var addPatientBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserId: String

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
        firestore.collection("doctor")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val doctorData = document.toObject(Doctor::class.java)
                    val patientIds = doctorData?.patientIds ?: emptyList()
                    fetchPatientDetails(patientIds)
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors
            }
    }

    private fun fetchPatientDetails(patientIds: List<String>) {
        val data = ArrayList<MyPatientsViewModel>()
        val recyclerview = findViewById<RecyclerView>(R.id.myPatientsRecyclerView)

        for (patientId in patientIds) {
            firestore.collection("patient")
                .document(patientId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val patient = document.toObject(Patient::class.java)
                        if (patient != null) {
                            val fullName = "${patient.firstName} ${patient.lastName}"
                            data.add(MyPatientsViewModel(R.drawable.ic_profile_icon, fullName, patient.id))
                            recyclerview.adapter?.notifyDataSetChanged()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                }
        }

        val adapter = MyPatientsAdapter(data) { patientId ->
            val intent = Intent(this, DoctorCheckPatientActivity::class.java)
            intent.putExtra("PATIENT_ID", patientId)
            startActivity(intent)
        }
        recyclerview.adapter = adapter
    }
}
