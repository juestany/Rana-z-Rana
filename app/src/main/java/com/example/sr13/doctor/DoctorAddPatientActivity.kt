package com.example.sr13.doctor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sr13.R
import com.example.sr13.firestore.Doctor
import com.example.sr13.firestore.Login
import com.example.sr13.firestore.Patient
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class DoctorAddPatientActivity : AppCompatActivity() {

    private lateinit var addPatientName: EditText
    private lateinit var addPatientLastName: EditText
    private lateinit var addPatientPESEL: EditText
    private lateinit var addPatientNumber: EditText
    private lateinit var addPatientAddress: EditText
    private lateinit var addPatientDate: EditText
    private lateinit var addPatientOperation: EditText
    private lateinit var previewImage: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var currentDoctorId: String
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_add_patient)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        currentDoctorId = auth.currentUser?.uid ?: ""

        addPatientName = findViewById(R.id.addPatientName)
        addPatientLastName = findViewById(R.id.addPatientLastName)
        addPatientPESEL = findViewById(R.id.addPatientPESEL)
        addPatientNumber = findViewById(R.id.addPatientNumber)
        addPatientAddress = findViewById(R.id.addPatientAddress)
        addPatientDate = findViewById(R.id.addPatientDate)
        addPatientOperation = findViewById(R.id.addPatientOperation)
        previewImage = findViewById(R.id.previewImage)

        val uploadImageBtn = findViewById<Button>(R.id.uploadImageBtn)
        uploadImageBtn.setOnClickListener { openImageChooser() }

        val addPatientBtn = findViewById<Button>(R.id.addPatientBtn)
        addPatientBtn.setOnClickListener { savePatient() }
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Wybierz zdjęcie"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            previewImage.setImageURI(imageUri)
        }
    }

    private fun savePatient() {
        val firstName = addPatientName.text.toString().trim()
        val lastName = addPatientLastName.text.toString().trim()
        val pesel = addPatientPESEL.text.toString().trim()
        val phoneNumber = addPatientNumber.text.toString().trim()
        val address = addPatientAddress.text.toString().trim()
        val birthDateString = addPatientDate.text.toString().trim()
        val operation = addPatientOperation.text.toString().trim()

        if (validateInput(firstName, lastName, pesel, phoneNumber, address, birthDateString, operation)) {
            val birthDate = convertToDate(birthDateString)
            if (birthDate != null) {
                val email = generatePatientEmail(firstName, lastName)
                val password = generateRandomPassword()

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            if (user != null) {
                                val patientId = user.uid

                                val patient = Patient(
                                    id = patientId,
                                    firstName = firstName,
                                    lastName = lastName,
                                    pesel = pesel,
                                    phoneNumber = phoneNumber,
                                    adress = address,
                                    birthDate = birthDate,
                                    operation = operation,
                                    doctorId = currentDoctorId
                                )

                                if (imageUri != null) {
                                    uploadImageToStorage(imageUri!!, patient, email, password)
                                } else {
                                    addPatientToFirestore(patient)
                                    savePatientLogin(email, password, "pacjent")
                                    navigateToSuccessScreen(email, password)
                                }
                            }
                        } else {
                            Log.e(TAG, "Failed to create user in Firebase Authentication", task.exception)
                            Toast.makeText(this, "Failed to create patient account", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun uploadImageToStorage(imageUri: Uri, patient: Patient, email: String, password: String) {
        val storageRef = storage.reference
        val imageId = UUID.randomUUID().toString()
        val imagesRef = storageRef.child("patient/$imageId")

        imagesRef.putFile(imageUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                patient.imageiD = imageId
                addPatientToFirestore(patient)
                savePatientLogin(email, password, "pacjent")
                navigateToSuccessScreen(email, password)
            } else {
                Log.e(TAG, "Failed to upload image to storage", task.exception)
            }
        }
    }

    private fun validateInput(firstName: String, lastName: String, pesel: String,
                              phoneNumber: String, address: String, birthDate: String, operation: String): Boolean {
        // Implement validation logic as per your requirements
        return true // Dummy validation, replace with actual logic
    }

    private fun convertToDate(dateString: String): Timestamp? {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val date = format.parse(dateString)
            Timestamp(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun addPatientToFirestore(patient: Patient) {
        firestore.collection("patient")
            .document(patient.id)
            .set(patient)
            .addOnSuccessListener {
                updateDoctorPatientList(patient.id)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding patient to Firestore", e)
            }
    }

    private fun updateDoctorPatientList(patientId: String) {
        firestore.collection("doctor")
            .document(currentDoctorId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val doctor = document.toObject(Doctor::class.java)
                    val updatedPatientIds = doctor?.patientIds?.toMutableList() ?: mutableListOf()
                    updatedPatientIds.add(patientId)

                    firestore.collection("doctor")
                        .document(currentDoctorId)
                        .update("patientIds", updatedPatientIds)
                        .addOnSuccessListener {
                            Log.d(TAG, "Doctor patient list updated successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error updating doctor patient list", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting doctor document", e)
            }
    }

    private fun savePatientLogin(email: String, password: String, role: String) {
        val login = Login(email = email, password = password, role = role)
        firestore.collection("login")
            .document(auth.currentUser?.uid ?: "")
            .set(login)
            .addOnSuccessListener {
                Log.d(TAG, "Patient login saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving patient login", e)
            }
    }

    private fun generatePatientEmail(firstName: String, lastName: String): String {
        val firstThreeLettersFirstName = if (firstName.length >= 3) firstName.substring(0, 3).lowercase(Locale.getDefault()) else firstName.lowercase(Locale.getDefault())
        val firstThreeLettersLastName = if (lastName.length >= 3) lastName.substring(0, 3).lowercase(Locale.getDefault()) else lastName.lowercase(Locale.getDefault())
        return "$firstThreeLettersFirstName$firstThreeLettersLastName@gmail.com"
    }

    private fun generateRandomPassword(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun navigateToSuccessScreen(email: String, password: String) {
        val intent = Intent(this, DoctorAddPatientSuccessActivity::class.java)
        intent.putExtra("patientLogin", email)
        intent.putExtra("patientPassword", password)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "DoctorAddPatientActivity"
        private const val PICK_IMAGE_REQUEST = 1
    }
}
