package com.example.sr13.doctor

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.sr13.LoginActivity
import com.example.sr13.R
import com.example.sr13.doctor.check_patient_rv.SubmittedReportsAdapter
import com.example.sr13.doctor.check_patient_rv.SubmittedReportsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.Locale

/**
 * Main activity for doctors, displaying their profile and today's submitted reports.
 * Provides navigation to the patient list and logout functionality.
 */
class DoctorMainActivity : AppCompatActivity() {

    // UI components
    private lateinit var doctorNameMain: TextView
    private lateinit var doctorRoleMain: TextView
    private lateinit var doctorProfilePicMain: ImageFilterView
    private lateinit var doctorSubmittedReportsRecyclerView: RecyclerView
    private lateinit var myPatientsBtn: Button
    private lateinit var logoutBtn: Button

    // Firebase components
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Data variables
    private lateinit var patientIds: List<String> // List of patient IDs associated with the doctor

    /**
     * Called when the activity is first created.
     * Initializes Firebase, binds UI components, and sets up listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_main)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Bind UI components
        doctorNameMain = findViewById(R.id.patientNameMain)
        doctorRoleMain = findViewById(R.id.doctorRoleMain)
        doctorProfilePicMain = findViewById(R.id.patientProfilePicMain)
        doctorSubmittedReportsRecyclerView = findViewById(R.id.doctorTodaySubmittedReportsRecyclerView)
        myPatientsBtn = findViewById(R.id.myPatientsBtn)
        logoutBtn = findViewById(R.id.logoutBtn)

        // Fetch doctor data and setup RecyclerView
        getDoctorData()
        setupRecyclerView()

        // Button to navigate to the "My Patients" screen
        myPatientsBtn.setOnClickListener {
            val intent = Intent(this@DoctorMainActivity, DoctorMyPatientsActivity::class.java)
            startActivity(intent)
        }

        // Logout button functionality
        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this@DoctorMainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Fetches doctor profile data from Firestore and populates the UI.
     * Also triggers fetching today's submitted reports.
     */
    private fun getDoctorData() {
        val userId = auth.currentUser?.uid

        userId?.let { uid ->
            firestore.collection("doctor")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        val title = document.getString("title")
                        val imageUrl = document.getString("imageId")
                        patientIds = document.get("patientIds") as List<String>? ?: listOf()

                        // Populate UI with doctor information
                        doctorNameMain.text = "$firstName $lastName"
                        doctorRoleMain.text = title

                        // Fetch and display doctor's profile picture
                        imageUrl?.let {
                            fetchImageFromFirebaseStorage(it)
                        }

                        // Fetch today's reports after patient IDs are loaded
                        fetchTodayReports()
                    } else {
                        Log.e("DoctorData", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DoctorData", "Error fetching doctor data", exception)
                }
        }
    }

    /**
     * Loads the doctor's profile image from Firebase Storage using the URL.
     *
     * @param imageUrl The URL of the image to be loaded.
     */
    private fun fetchImageFromFirebaseStorage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(doctorProfilePicMain)
    }

    /**
     * Sets up the RecyclerView for displaying today's submitted reports.
     */
    private fun setupRecyclerView() {
        doctorSubmittedReportsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Fetches today's submitted reports for the doctor's patients from Firestore.
     */
    private fun fetchTodayReports() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        Log.d("DoctorMainActivity", "Today's date: $today")

        firestore.collection("reports")
            .whereEqualTo("date", today)
            .get()
            .addOnSuccessListener { documents ->
                val reportsList = mutableListOf<SubmittedReportsViewModel>()

                // Iterate through today's reports
                for (document in documents) {
                    val patientId = document.getString("userId") ?: ""
                    if (patientIds.contains(patientId)) {
                        val reportDate = document.getString("date") ?: ""
                        val reportId = document.id

                        // Fetch patient details to get the full name
                        firestore.collection("patient")
                            .document(patientId)
                            .get()
                            .addOnSuccessListener { patientDocument ->
                                if (patientDocument.exists()) {
                                    val firstName = patientDocument.getString("firstName") ?: ""
                                    val lastName = patientDocument.getString("lastName") ?: ""
                                    val fullName = "$firstName $lastName"

                                    // Add report details to the list
                                    val reportModel = SubmittedReportsViewModel(
                                        R.drawable.ic_paper_icon,
                                        fullName,
                                        reportDate,
                                        reportId
                                    )
                                    reportsList.add(reportModel)

                                    // Update RecyclerView adapter
                                    doctorSubmittedReportsRecyclerView.adapter = SubmittedReportsAdapter(reportsList) { reportId ->
                                        val intent = Intent(this@DoctorMainActivity, DoctorCheckPatientReportActivity::class.java)
                                        intent.putExtra("REPORT_ID", reportId)
                                        startActivity(intent)
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("DoctorMainActivity", "Error fetching patient details", e)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DoctorMainActivity", "Error fetching today's reports", exception)
            }
    }
}