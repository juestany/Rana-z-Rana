package com.example.sr13.doctor

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.sr13.R
import com.example.sr13.firestore.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorCheckPatientReportActivity : AppCompatActivity() {

    //TODO: make layout prettier

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserId: String

    private lateinit var patientReportDate: TextView
    private lateinit var patientOperationDesc: TextView
    private lateinit var imageView: ImageView
    private lateinit var patientComment: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_check_patient_report)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        patientReportDate = findViewById(R.id.patientReportDate)
        patientOperationDesc = findViewById(R.id.patientOperationDesc)
        imageView = findViewById(R.id.imageView)
        patientComment = findViewById(R.id.patientComment)

        val reportId = intent.getStringExtra("REPORT_ID") ?: ""
        loadReportData(reportId)
    }

    private fun loadReportData(reportId: String) {
        firestore.collection("reports")
            .document(reportId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val report = document.toObject(Report::class.java)
                    if (report != null) {
                        displayReportDetails(report)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors
            }
    }

    private fun displayReportDetails(report: Report) {
        patientReportDate.text = report.date
        // Load operation description from patient collection
        fetchOperationDescription(report.userId) { operationDescription ->
            patientOperationDesc.text = operationDescription
        }
        // Load image using Glide
        Glide.with(this)
            .load(report.imageUrl)
            .into(imageView)
        patientComment.text = report.comment
    }

    private fun fetchOperationDescription(userId: String, callback: (String) -> Unit) {
        firestore.collection("patient")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val operationDescription = document.getString("operation") ?: ""
                    callback.invoke(operationDescription)
                } else {
                    callback.invoke("")
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors
                callback.invoke("")
            }
    }
}
