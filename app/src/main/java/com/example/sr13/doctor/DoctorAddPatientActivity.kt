package com.example.sr13.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.sr13.R
import com.example.sr13.firestore.Doctor
import com.example.sr13.firestore.Patient
import com.example.sr13.firestore.Login
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class DoctorAddPatientActivity : AppCompatActivity() {

    private lateinit var addPatientName: EditText
    private lateinit var addPatientLastName: EditText
    private lateinit var addPatientPESEL: EditText
    private lateinit var addPatientNumber: EditText
    private lateinit var addPatientAddress: EditText
    private lateinit var addPatientDate: EditText
    private lateinit var addPatientImageBtn: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentDoctorId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_add_patient)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentDoctorId = auth.currentUser?.uid ?: ""

        addPatientName = findViewById(R.id.addPatientName)
        addPatientLastName = findViewById(R.id.addPatientLastName)
        addPatientPESEL = findViewById(R.id.addPatientPESEL)
        addPatientNumber = findViewById(R.id.addPatientNumber)
        addPatientAddress = findViewById(R.id.addPatientAddress)
        addPatientDate = findViewById(R.id.addPatientDate)
        addPatientImageBtn = findViewById(R.id.addPatientImageBtn)

        val addPatientBtn = findViewById<Button>(R.id.addPatientBtn)
        addPatientBtn.setOnClickListener {
            val firstName = addPatientName.text.toString().trim()
            val lastName = addPatientLastName.text.toString().trim()
            val pesel = addPatientPESEL.text.toString().trim()
            val phoneNumber = addPatientNumber.text.toString().trim()
            val address = addPatientAddress.text.toString().trim()
            val birthDateString = addPatientDate.text.toString().trim()

            if (validateInput(firstName, lastName, pesel, phoneNumber, address, birthDateString)) {
                val birthDate = convertToDate(birthDateString)
                if (birthDate != null) {
                    val email = generatePatientEmail(firstName, lastName)
                    val patient = Patient(
                        id = UUID.randomUUID().toString(),
                        firstName = firstName,
                        lastName = lastName,
                        pesel = pesel,
                        phoneNumber = phoneNumber,
                        adress = address,
                        birthDate = birthDate,
                        doctorId = currentDoctorId
                    )
                    val password = generateRandomPassword()

                    addPatientToFirestore(patient)
                    savePatientLogin(email, password, "patient")
                }
            }
        }
    }

    private fun generatePatientEmail(firstName: String, lastName: String): String {
        val firstThreeLettersFirstName = if (firstName.length >= 3) firstName.substring(0, 3).toLowerCase(Locale.getDefault()) else firstName.toLowerCase(Locale.getDefault())
        val firstThreeLettersLastName = if (lastName.length >= 3) lastName.substring(0, 3).toLowerCase(Locale.getDefault()) else lastName.toLowerCase(Locale.getDefault())
        return "$firstThreeLettersFirstName$firstThreeLettersLastName@gmail.com"
    }

    private fun validateInput(firstName: String, lastName: String, pesel: String,
                              phoneNumber: String, address: String, birthDate: String): Boolean {
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
        firestore.collection("patients")
            .document(patient.id)
            .set(patient)
            .addOnSuccessListener {
                updateDoctorPatientList(patient.id)
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    private fun updateDoctorPatientList(patientId: String) {
        firestore.collection("doctors")
            .document(currentDoctorId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val doctor = document.toObject(Doctor::class.java)
                    val updatedPatientIds = doctor?.patientIds?.toMutableList() ?: mutableListOf()
                    updatedPatientIds.add(patientId)

                    firestore.collection("doctors")
                        .document(currentDoctorId)
                        .update("patientIds", updatedPatientIds)
                        .addOnSuccessListener {
                            startActivity(Intent(this, DoctorMyPatientsActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            // Handle failure
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    private fun savePatientLogin(email: String, password: String, role: String) {
        val login = Login(email = email, password = password, role = "patient")
        firestore.collection("logins")
            .document(auth.currentUser?.uid ?: "")
            .set(login)
            .addOnSuccessListener {
                // Handle success if needed
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    private fun generateRandomPassword(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
