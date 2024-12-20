package com.example.sr13.doctor

import android.annotation.SuppressLint
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
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for adding a new patient by a doctor.
 * Handles user input, image upload, patient creation, and Firestore integration.
 */
class DoctorAddPatientActivity : AppCompatActivity() {

    // Constants
    private val PLACE_PICKER_REQUEST = 1
    private val PICK_IMAGE_REQUEST = 71

    // UI components
    private lateinit var openMapButton: Button
    private lateinit var addPatientName: EditText
    private lateinit var addPatientLastName: EditText
    private lateinit var addPatientPESEL: EditText
    private lateinit var addPatientNumber: EditText
    private lateinit var addPatientAddress: EditText
    private lateinit var addPatientDate: EditText
    private lateinit var addPatientOperation: EditText
    private lateinit var previewImage: ImageView

    // Firebase components
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    // Current doctor's ID
    private lateinit var currentDoctorId: String

    // Selected image URI
    private var imageUri: Uri? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_add_patient)

        // Initialize Places API for address selection
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        currentDoctorId = auth.currentUser?.uid ?: ""

        // Bind UI components
        addPatientName = findViewById(R.id.addPatientName)
        openMapButton = findViewById(R.id.openMapButtonPatient)
        addPatientLastName = findViewById(R.id.addPatientLastName)
        addPatientPESEL = findViewById(R.id.addPatientPESEL)
        addPatientNumber = findViewById(R.id.addPatientNumber)
        addPatientAddress = findViewById(R.id.addPatientAddress)
        addPatientDate = findViewById(R.id.addPatientDate)
        addPatientOperation = findViewById(R.id.addPatientOperation)
        previewImage = findViewById(R.id.previewImage)

        // Set up button listeners
        openMapButton.setOnClickListener { openPlacePicker() }
        findViewById<Button>(R.id.uploadImageBtn).setOnClickListener { openImageChooser() }
        findViewById<Button>(R.id.addPatientBtn).setOnClickListener { savePatient() }
    }

    /**
     * Opens the Google Places Autocomplete UI to select an address.
     */
    private fun openPlacePicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
        startActivityForResult(intent, PLACE_PICKER_REQUEST)
    }

    /**
     * Opens a file chooser to select an image from the device.
     */
    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Wybierz zdjęcie"), PICK_IMAGE_REQUEST)
    }

    /**
     * Handles the result from the Place Picker and Image Chooser.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            previewImage.setImageURI(imageUri)
        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    addPatientAddress.setText(place.address)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Validates input fields, creates a new patient, and saves it to Firestore.
     */
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
                patient.imageId = imageId
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
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update doctor's patient list", e)
            }
    }

    private fun generatePatientEmail(firstName: String, lastName: String): String {
        val uuid = UUID.randomUUID().toString().substring(0, 8)
        return "${firstName.lowercase(Locale.getDefault())}.${lastName.lowercase(Locale.getDefault())}.$uuid@patientapp.com"
    }

    private fun generateRandomPassword(): String {
        return UUID.randomUUID().toString().substring(0, 8)
    }

    private fun savePatientLogin(email: String, password: String, role: String) {
        val login = Login(email, password, role)
        firestore.collection("login")
            .document(email)
            .set(login)
            .addOnSuccessListener {
                Log.d(TAG, "Patient login data saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save login data", e)
            }
    }

    private fun navigateToSuccessScreen(email: String, password: String) {
        val intent = Intent(this, DoctorAddPatientSuccessActivity::class.java)
        intent.putExtra("email", email)
        intent.putExtra("password", password)
        startActivity(intent)
        finish()
    }


    companion object {
        private const val TAG = "DoctorAddPatientActivity"
        private const val PICK_IMAGE_REQUEST = 71
    }
}
