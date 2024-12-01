package com.example.sr13

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.sr13.firestore.Doctor
import com.example.sr13.firestore.DoctorFirestoreDatabaseOperations
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteActivity

/**
 * Handles the user registration functionality for doctors.
 * Integrates Google Places API for address input and Firebase Authentication for account creation.
 */
class RegisterActivity : BaseActivity() {

    private val PLACE_PICKER_REQUEST = 1
    private lateinit var registerButton: Button
    private lateinit var openMapButton: Button
    private lateinit var inputEmail: EditText
    private lateinit var inputFirstName: EditText
    private lateinit var inputLastName: EditText
    private lateinit var inputPhoneNumber: EditText
    private lateinit var inputAddress: EditText
    private lateinit var inputTitle: EditText
    private lateinit var inputPassword: EditText
    private lateinit var inputRepPass: EditText

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up click listeners, and integrates Google Places API.
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key))
        }

        registerButton = findViewById(R.id.registerButton)
        openMapButton = findViewById(R.id.openMapButton)
        inputEmail = findViewById(R.id.registerEmail)
        inputFirstName = findViewById(R.id.registerFirstName)
        inputLastName = findViewById(R.id.registerLastName)
        inputPhoneNumber = findViewById(R.id.registerPhoneNumber)
        inputAddress = findViewById(R.id.registerAddress)
        inputTitle = findViewById(R.id.registerTitle)
        inputPassword = findViewById(R.id.registerPassword)
        inputRepPass = findViewById(R.id.registerPassword2Repeat)

        openMapButton.setOnClickListener {
            openPlacePicker()
        }

        registerButton.setOnClickListener {
            registerDoctor()
        }
    }

    /**
     * Launches the Google Places picker for selecting an address.
     */
    private fun openPlacePicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(this)
        startActivityForResult(intent, PLACE_PICKER_REQUEST)
    }

    /**
     * Handles results from external activities (e.g., Google Places picker).
     * Sets the selected address in the input field or displays an error if the operation failed.
     *
     * @param requestCode The request code identifying the activity result.
     * @param resultCode The result code from the activity.
     * @param data The returned data (e.g., selected place).
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PLACE_PICKER_REQUEST) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    inputAddress.setText(place.address)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    Toast.makeText(this, "Error: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Validates all the registration input fields.
     * Ensures that no field is empty and passwords match.
     *
     * @return True if all fields are valid; false otherwise.
     */
    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(inputEmail.text.toString().trim()) -> {
                showErrorSnackBar("Proszę wpisać adres e-mail.", true)
                false
            }
            TextUtils.isEmpty(inputFirstName.text.toString().trim()) -> {
                showErrorSnackBar("Proszę wpisać imię.", true)
                false
            }
            TextUtils.isEmpty(inputLastName.text.toString().trim()) -> {
                showErrorSnackBar("Proszę wpisać nazwisko.", true)
                false
            }
            TextUtils.isEmpty(inputPhoneNumber.text.toString().trim()) -> {
                showErrorSnackBar("Proszę wpisać numer telefonu.", true)
                false
            }
            TextUtils.isEmpty(inputAddress.text.toString().trim()) -> {
                showErrorSnackBar("Proszę wpisać adres.", true)
                false
            }
            TextUtils.isEmpty(inputTitle.text.toString().trim()) -> {
                showErrorSnackBar("Proszę wpisać tytuł.", true)
                false
            }
            TextUtils.isEmpty(inputPassword.text.toString().trim()) -> {
                showErrorSnackBar("Proszę wpisać hasło.", true)
                false
            }
            TextUtils.isEmpty(inputRepPass.text.toString().trim()) -> {
                showErrorSnackBar("Proszę powtórzyć hasło.", true)
                false
            }
            inputPassword.text.toString().trim() != inputRepPass.text.toString().trim() -> {
                showErrorSnackBar("Hasła muszą się zgadzać.", true)
                false
            }
            else -> true
        }
    }

    /**
     * Handles the registration process for a doctor.
     * Validates input fields, creates a Firebase Authentication user, and registers the doctor in Firestore.
     */
    private fun registerDoctor() {
        if (validateRegisterDetails()) {
            showProgressDialog("Proszę czekać...")

            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!

                        val doctor = Doctor(
                            id = firebaseUser.uid,
                            firstName = inputFirstName.text.toString().trim(),
                            lastName = inputLastName.text.toString().trim(),
                            phoneNumber = inputPhoneNumber.text.toString().trim(),
                            address = inputAddress.text.toString().trim(),  // Zapisz adres z Google Places API
                            title = inputTitle.text.toString().trim(),
                            imageId = "default_image_id",
                            patientIds = listOf()
                        )

                        DoctorFirestoreDatabaseOperations().registerDoctor(this, doctor)
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }


        }
    }


    /**
     * Callback for successful user registration.
     * Displays a success message, signs out the user, and finishes the activity.
     */
    fun userRegistrationSuccess() {
        hideProgressDialog()
        Toast.makeText(this@RegisterActivity, "Rejestracja zakończona sukcesem.", Toast.LENGTH_LONG).show()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}
