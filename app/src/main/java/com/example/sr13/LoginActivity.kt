package com.example.sr13

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.example.sr13.doctor.DoctorMainActivity
import com.example.sr13.patient.PatientMainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * Handles the user login functionality.
 * Connects to Firebase for user authentication and determines the user's role.
 */
class LoginActivity : BaseActivity(), View.OnClickListener {
    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var loginButton: Button? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    /**
     * Called when the activity is first created.
     * Initializes Firebase services and UI components.
     * Sets up the Firebase Emulator Suite when running in debug mode.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        if (BuildConfig.DEBUG) {
            FirebaseDatabase.getInstance().useEmulator("10.0.2.2", 9003)
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199)
        }

        inputEmail = findViewById(R.id.loginEmail)
        inputPassword = findViewById(R.id.loginPassword)
        loginButton = findViewById(R.id.loginButton)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() // Dodano inicjalizację zmiennej auth
        loginButton?.setOnClickListener {
            logInRegisteredUser()
        }
    }

    /**
     * Handles button click events.
     * Navigates to the registration screen when the register text view is clicked.
     */
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.registerUserTextView -> {
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * Validates the user's login details.
     * Checks if the email and password fields are not empty.
     * Shows a snackbar message if validation fails.
     *
     * @return True if both fields are valid; false otherwise.
     */
    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(inputEmail?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(inputPassword?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                showErrorSnackBar("Your details are valid", false)
                true
            }
        }
    }

    /**
     * Logs in a registered user using Firebase Authentication.
     * Validates the user's credentials before attempting login.
     * On successful login, navigates to the appropriate activity based on the user's role.
     */
    private fun logInRegisteredUser() {
        if (validateLoginDetails()) {
            val email = inputEmail?.text.toString().trim() { it <= ' ' }
            val password = inputPassword?.text.toString().trim() { it <= ' ' }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User logged in successfully")
                        goToNextActivity()
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    /**
     * Determines the user's role (e.g., doctor or patient) and navigates to the appropriate activity.
     * Fetches the user's role from Firestore based on their ID.
     * Handles navigation and displays errors if the role is unknown or the data fetch fails.
     */
    private fun goToNextActivity() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.email.toString()
        val userId = auth.currentUser?.uid

        userId?.let { uid ->
            firestore.collection("login")
                .document(uid)
                .get()
                .addOnSuccessListener { appUserDocument ->
                    val role = appUserDocument.getString("role")

                    when (role) {
                        "lekarz" -> {
                            val intent = Intent(this, DoctorMainActivity::class.java)
                            intent.putExtra("uID", uid)
                            startActivity(intent)
                            finish()
                        }
                        "pacjent" -> {
                            val intent = Intent(this, PatientMainActivity::class.java)
                            intent.putExtra("uID", uid)
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            showErrorSnackBar("Unknown role", true)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting document", e)
                    showErrorSnackBar(e.message.toString(), true)
                }
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
