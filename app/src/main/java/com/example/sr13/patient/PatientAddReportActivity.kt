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
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PatientAddReportActivity : AppCompatActivity() {

    private lateinit var previewImage: AppCompatImageView
    private lateinit var patientComment: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var currentUserId: String // To store current user's ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_add_report)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        previewImage = findViewById(R.id.previewImage)
        patientComment = findViewById(R.id.patientComment)

        findViewById<Button>(R.id.uploadImageBtn).setOnClickListener {
            startImagePicker()
        }

        findViewById<Button>(R.id.myPatientsBtn).setOnClickListener {
            saveReport()
        }

        loadPatientData()
    }

    private fun startImagePicker() {
        val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        changeImage.launch(pickImage)
    }

    private val changeImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val imgUri = data?.data
            previewImage.setImageURI(imgUri)
            previewImage.tag = imgUri // Store image URI as tag for future reference
        }
    }

    private fun saveReport() {
        // Check if an image has been selected
        val imgUri = previewImage.tag as? Uri
        if (imgUri == null) {
            // Handle case where no image has been selected
            Log.e(TAG, "No image selected")
            return
        }

        // Check if report with the current date already exists
        val currentDate = getCurrentDate()

        firestore.collection("reports")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("date", currentDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Report with this date already exists
                    Log.w(TAG, "Report for today already exists")
                    Toast.makeText(this, "Raport został już dodany tego dnia", Toast.LENGTH_SHORT).show()
                } else {
                    // Upload image to Firebase Storage
                    uploadImageToStorage(imgUri)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking for existing report", e)
            }
    }

    private fun uploadImageToStorage(imageUri: Uri?) {
        imageUri?.let {
            val storageRef = storage.reference
            val imageId = UUID.randomUUID().toString()
            val imagesRef = storageRef.child("reports/$imageId")

            val uploadTask = imagesRef.putFile(imageUri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imagesRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    saveReportToFirestore(imageId, downloadUri.toString())
                } else {
                    Log.e(TAG, "Failed to upload image to storage")
                    Toast.makeText(this, "Nie udało się przesłać obrazu do storage", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveReportToFirestore(imageId: String, imageUrl: String) {
        val reportId = UUID.randomUUID().toString() // Generowanie unikalnego ID raportu
        val report = hashMapOf(
            "reportId" to reportId, // Dodanie reportId do dokumentu
            "imageId" to imageId,
            "userId" to currentUserId,
            "imageUrl" to imageUrl,
            "comment" to patientComment.text.toString(),
            "date" to getCurrentDate()
        )

        firestore.collection("reports")
            .document(reportId) // Ustawienie reportId jako ID dokumentu
            .set(report)
            .addOnSuccessListener {
                Log.d(TAG, "Report added with ID: $reportId")
                Toast.makeText(this, "Raport został dodany pomyślnie", Toast.LENGTH_SHORT).show()

                // Pobranie danych pacjenta, aby uzyskać doctorId
                firestore.collection("patient")
                    .document(currentUserId)
                    .get()
                    .addOnSuccessListener { patientSnapshot ->
                        if (patientSnapshot != null) {
                            val patient = patientSnapshot.toObject(Patient::class.java)
                            val doctorId = patient?.doctorId

                            // Wysłanie powiadomienia do lekarza
                            sendNotificationToDoctor(doctorId)
                        } else {
                            Log.e(TAG, "No such document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting patient document: ", exception)
                    }

                finish() // Zamknięcie activity po pomyślnym dodaniu raportu
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding report", e)
                Toast.makeText(this, "Nie udało się dodać raportu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendNotificationToDoctor(doctorId: String?) {
        if (doctorId.isNullOrEmpty()) {
            Log.e(TAG, "Doctor ID is null or empty")
            return
        }

        val payload = hashMapOf(
            "notification" to hashMapOf(
                "title" to "Nowy raport dodany",
                "body" to "Nowy raport został dodany przez pacjenta."
            ),
            "data" to hashMapOf(
                "patientId" to currentUserId // Przekazanie ID pacjenta
            )
        )

        // Zakładam, że masz funkcję do wysyłania powiadomień do lekarza w swoim backendzie
        // Tutaj jest miejsce na implementację tej funkcji
        firestore.collection("notifications")
            .document(doctorId)
            .set(payload)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Notification sent successfully to doctor: $doctorId")
                } else {
                    Log.e(TAG, "Failed to send notification to doctor: $doctorId", task.exception)
                }
            }
    }

    private fun loadPatientData() {
        val user = auth.currentUser
        user?.let {
            currentUserId = it.uid
            firestore.collection("patient")
                .document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val patient = document.toObject(Patient::class.java)
                        findViewById<TextView>(R.id.patientOperationDesc).text = patient?.operation ?: "Brak danych"
                    } else {
                        Log.e(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting documents: ", exception)
                }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    companion object {
        private const val TAG = "PatientAddReportActivity"
    }
}
