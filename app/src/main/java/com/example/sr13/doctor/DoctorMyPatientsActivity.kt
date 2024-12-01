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

/**
 * Activity for doctors to view and manage their list of patients.
 * Displays patients in a RecyclerView and allows navigation to add a new patient or view patient details.
 */
class DoctorMyPatientsActivity : AppCompatActivity() {

    // UI components
    private lateinit var addPatientBtn: Button

    // Firebase components
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Current user's ID
    private lateinit var currentUserId: String

    /**
     * Called when the activity is first created.
     * Initializes Firebase, sets up RecyclerView, and handles button actions.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_my_patients)

        // Bind UI components
        addPatientBtn = findViewById(R.id.addPatientBtn)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        // Set up RecyclerView
        val recyclerview = findViewById<RecyclerView>(R.id.myPatientsRecyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)

        // Load patients data
        loadPatientsData()

        // Navigate to add patient activity when the button is clicked
        addPatientBtn.setOnClickListener {
            val intent = Intent(this@DoctorMyPatientsActivity, DoctorAddPatientActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Fetches the list of patient IDs associated with the doctor from Firestore.
     * Calls [fetchPatientDetails] to retrieve patient details.
     */
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
                // Handle any errors, e.g., log them
            }
    }

    /**
     * Retrieves detailed information for each patient from Firestore and updates the RecyclerView.
     *
     * @param patientIds List of patient IDs to fetch details for.
     */
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
                            recyclerview.adapter?.notifyDataSetChanged() // Notify adapter to refresh the view
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle any errors, e.g., log them
                }
        }

        // Initialize the RecyclerView adapter
        val adapter = MyPatientsAdapter(data) { patientId ->
            val intent = Intent(this, DoctorCheckPatientActivity::class.java)
            intent.putExtra("PATIENT_ID", patientId)
            startActivity(intent)
        }
        recyclerview.adapter = adapter
    }
}