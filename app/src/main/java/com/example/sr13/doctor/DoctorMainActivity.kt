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

class DoctorMainActivity : AppCompatActivity() {

    private lateinit var doctorNameMain: TextView
    private lateinit var doctorRoleMain: TextView
    private lateinit var doctorProfilePicMain: ImageFilterView
    private lateinit var doctorSubmittedReportsRecyclerView: RecyclerView
    private lateinit var myPatientsBtn: Button
    private lateinit var logoutBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_main)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        doctorNameMain = findViewById(R.id.patientNameMain)
        doctorRoleMain = findViewById(R.id.doctorRoleMain)
        doctorProfilePicMain = findViewById(R.id.patientProfilePicMain)
        doctorSubmittedReportsRecyclerView = findViewById(R.id.doctorTodaySubmittedReportsRecyclerView)
        myPatientsBtn = findViewById(R.id.myPatientsBtn)
        logoutBtn = findViewById(R.id.logoutBtn)

        // Set up initial values or listeners here
        getDoctorData()
        setupRecyclerView()

        myPatientsBtn.setOnClickListener() {
            val intent = Intent(this@DoctorMainActivity, DoctorMyPatientsActivity::class.java)
            startActivity(intent)
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this@DoctorMainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

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
                        val address = document.getString("adress") // Note: fixed typo to "address"
                        val phoneNumber = document.getString("phoneNumber")
                        val imageUrl = document.getString("imageId")

                        doctorNameMain.text = "$firstName $lastName"
                        doctorRoleMain.text = title

                        Log.d("DoctorData", "Fetched imageUrl: $imageUrl")

                        // Fetch and display the image from Firebase Storage using URL
                        imageUrl?.let {
                            fetchImageFromFirebaseStorage(it)
                        }
                    } else {
                        Log.e("DoctorData", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DoctorData", "Error fetching doctor data", exception)
                }
        }
    }

    private fun fetchImageFromFirebaseStorage(imageUrl: String) {
        // Load the image into ImageFilterView using Glide
        Glide.with(this)
            .load(imageUrl)
            .into(doctorProfilePicMain)
    }

    private fun setupRecyclerView() {
        doctorSubmittedReportsRecyclerView.layoutManager = LinearLayoutManager(this)
        fetchTodayReports()
    }

    private fun fetchTodayReports() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        Log.d("DoctorMainActivity", "Today's date: $today")

        firestore.collection("reports")
            .whereEqualTo("date", today)
            .get()
            .addOnSuccessListener { documents ->
                val reportsList = mutableListOf<SubmittedReportsViewModel>()
                for (document in documents) {
                    val patientId = document.getString("userId") ?: ""
                    val reportDate = document.getString("date") ?: ""
                    val reportId = document.id

                    firestore.collection("patient")
                        .document(patientId)
                        .get()
                        .addOnSuccessListener { patientDocument ->
                            if (patientDocument.exists()) {
                                val firstName = patientDocument.getString("firstName") ?: ""
                                val lastName = patientDocument.getString("lastName") ?: ""
                                val fullName = "$firstName $lastName"

                                val reportModel = SubmittedReportsViewModel(
                                    R.drawable.ic_paper_icon,
                                    fullName,
                                    reportDate,
                                    reportId
                                )
                                reportsList.add(reportModel)
                                doctorSubmittedReportsRecyclerView.adapter?.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("DoctorMainActivity", "Error fetching patient details", e)
                        }
                }
                doctorSubmittedReportsRecyclerView.adapter = SubmittedReportsAdapter(reportsList) { reportId ->
                    val intent = Intent(this@DoctorMainActivity, DoctorCheckPatientReportActivity::class.java)
                    intent.putExtra("REPORT_ID", reportId)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DoctorMainActivity", "Error fetching today's reports", exception)
            }
    }

}
