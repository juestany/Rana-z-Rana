package com.example.sr13.patient

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.example.sr13.R
import com.example.sr13.firestore.Patient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Activity for patients to add and submit a new report.
 * Handles uploading images, adding comments, and saving data to Firestore.
 */
class PatientAddReportActivity : AppCompatActivity() {

    // UI Components
    private lateinit var previewImage: AppCompatImageView
    private lateinit var patientComment: EditText
    private lateinit var todayDate: TextView

    // Firebase components
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // Current user's ID
    private lateinit var currentUserId: String

    /**
     * Called when the activity is first created.
     * Initializes Firebase, binds UI components, and sets up listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_add_report)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Bind UI components
        previewImage = findViewById(R.id.previewImage)
        patientComment = findViewById(R.id.patientComment)
        todayDate = findViewById(R.id.patientReportDate)

        // Set today's date
        todayDate.text = getCurrentDate()

        // Set up listeners
        findViewById<Button>(R.id.uploadImageBtn).setOnClickListener {
            startImagePicker()
        }
        findViewById<Button>(R.id.myPatientsBtn).setOnClickListener {
            saveReport()
        }

        // Load additional patient data
        loadPatientData()
    }

    /**
     * Opens the device's image picker to select an image.
     */
    private fun startImagePicker() {
        val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        changeImage.launch(pickImage)
    }

    private val changeImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imgUri = result.data?.data
            previewImage.setImageURI(imgUri)
            previewImage.tag = imgUri // Store image URI as tag
        }
    }

    /**
     * Saves the report to Firestore after validating and uploading the image.
     */
    private fun saveReport() {
        val imgUri = previewImage.tag as? Uri
        if (imgUri == null) {
            Log.e(TAG, "No image selected")
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = getCurrentDate()
        firestore.collection("reports")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("date", currentDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    Log.w(TAG, "Report for today already exists")
                    Toast.makeText(this, "Report already submitted for today", Toast.LENGTH_SHORT).show()
                } else {
                    uploadImageToStorage(imgUri)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking for existing report", e)
            }
    }

    /**
     * Uploads the selected image to Firebase Storage.
     *
     * @param imageUri The URI of the image to upload.
     */
    private fun uploadImageToStorage(imageUri: Uri?) {
        imageUri?.let {
            val storageRef = storage.reference
            val imageId = UUID.randomUUID().toString()
            val imagesRef = storageRef.child("reports/$imageId")

            imagesRef.putFile(imageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imagesRef.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result.toString()
                        saveReportToFirestore(imageId, downloadUri)
                    } else {
                        Log.e(TAG, "Failed to upload image")
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    /**
     * Saves the report data to Firestore.
     *
     * @param imageId The ID of the uploaded image.
     * @param imageUrl The URL of the uploaded image.
     */
    private fun saveReportToFirestore(imageId: String, imageUrl: String) {
        val reportId = UUID.randomUUID().toString()
        val report = hashMapOf(
            "reportId" to reportId,
            "imageId" to imageId,
            "userId" to currentUserId,
            "imageUrl" to imageUrl,
            "comment" to patientComment.text.toString(),
            "date" to getCurrentDate()
        )

        firestore.collection("reports").document(reportId).set(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding report", e)
                Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Fetches patient-specific data from Firestore.
     */
    private fun loadPatientData() {
        auth.currentUser?.uid?.let { uid ->
            currentUserId = uid
            firestore.collection("patient").document(currentUserId).get()
                .addOnSuccessListener { document ->
                    val patient = document.toObject(Patient::class.java)
                    findViewById<TextView>(R.id.patientOperationDesc).text = patient?.operation ?: "No data"
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching patient data", e)
                }
        }
    }

    /**
     * Returns the current date in the format "dd/MM/yyyy".
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    companion object {
        private const val TAG = "PatientAddReportActivity"
    }
}