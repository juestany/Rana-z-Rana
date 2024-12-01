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

/**
 * Activity for doctors to view the details of a patient's report.
 * Displays the report's date, operation description, image, and patient comments.
 */
class DoctorCheckPatientReportActivity : AppCompatActivity() {

    // Firebase components
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserId: String

    // UI components
    private lateinit var patientReportDate: TextView
    private lateinit var patientOperationDesc: TextView
    private lateinit var imageView: ImageView
    private lateinit var patientComment: TextView

    /**
     * Called when the activity is first created.
     * Initializes Firebase, binds UI components, and loads report data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_check_patient_report)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        // Bind UI components
        patientReportDate = findViewById(R.id.patientReportDate)
        patientOperationDesc = findViewById(R.id.patientOperationDesc)
        imageView = findViewById(R.id.imageView)
        patientComment = findViewById(R.id.patientComment)

        // Get report ID from the intent and load report data
        val reportId = intent.getStringExtra("REPORT_ID") ?: ""
        loadReportData(reportId)
    }

    /**
     * Fetches the report details from Firestore using the report ID.
     * Calls [displayReportDetails] if the report data is successfully retrieved.
     *
     * @param reportId The ID of the report to load.
     */
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
                Log.e("DoctorCheckPatientReport", "Error loading report: ${e.message}", e)
            }
    }

    /**
     * Displays the details of a report on the UI.
     * Fetches additional information such as operation description and loads the report image.
     *
     * @param report The report object containing the details.
     */
    private fun displayReportDetails(report: Report) {
        // Set the report date
        patientReportDate.text = report.date

        // Fetch and set the operation description
        fetchOperationDescription(report.userId) { operationDescription ->
            patientOperationDesc.text = operationDescription
        }

        // Load the report image using Glide
        Glide.with(this)
            .load(report.imageUrl)
            .into(imageView)

        // Set the patient's comment
        patientComment.text = report.comment
    }

    /**
     * Fetches the operation description for a patient from Firestore.
     * Invokes a callback with the operation description or an empty string if not found.
     *
     * @param userId The ID of the patient whose operation description is needed.
     * @param callback A callback function to handle the operation description.
     */
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
                Log.e("DoctorCheckPatientReport", "Error fetching operation description: ${e.message}", e)
                callback.invoke("")
            }
    }
}