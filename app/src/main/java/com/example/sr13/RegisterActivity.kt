package com.example.sr13

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.sr13.firestore.Doctor
import com.example.sr13.firestore.DoctorFirestoreDatabaseOperations
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : BaseActivity() {

    private var registerButton: Button? = null
    private var inputEmail: EditText? = null
    private var inputFirstName: EditText? = null
    private var inputLastName: EditText? = null
    private var inputPhoneNumber: EditText? = null
    private var inputAddress: EditText? = null
    private var inputTitle: EditText? = null
    private var inputPassword: EditText? = null
    private var inputRepPass: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        registerButton = findViewById(R.id.registerButton)
        inputEmail = findViewById(R.id.registerEmail)
        inputFirstName = findViewById(R.id.registerFirstName)
        inputLastName = findViewById(R.id.registerLastName)
        inputPhoneNumber = findViewById(R.id.registerPhoneNumber)
        inputAddress = findViewById(R.id.registerAddress)
        inputTitle = findViewById(R.id.registerTitle)
        inputPassword = findViewById(R.id.registerPassword)
        inputRepPass = findViewById(R.id.registerPassword2Repeat)

        registerButton?.setOnClickListener {
            registerDoctor()
        }
    }

    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(inputEmail?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Wprowadź adres e-mail.", true)
                false
            }
            TextUtils.isEmpty(inputFirstName?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Wprowadź imię.", true)
                false
            }
            TextUtils.isEmpty(inputLastName?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Wprowadź nazwisko.", true)
                false
            }
            TextUtils.isEmpty(inputPhoneNumber?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Wprowadź numer telefonu.", true)
                false
            }
            TextUtils.isEmpty(inputAddress?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Wprowadź adres.", true)
                false
            }
            TextUtils.isEmpty(inputTitle?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Wprowadź tytuł.", true)
                false
            }
            TextUtils.isEmpty(inputPassword?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Wprowadź hasło.", true)
                false
            }
            TextUtils.isEmpty(inputRepPass?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar("Powtórz hasło.", true)
                false
            }
            inputPassword?.text.toString().trim { it <= ' ' } != inputRepPass?.text.toString().trim { it <= ' ' } -> {
                showErrorSnackBar("Hasła muszą być takie same.", true)
                false
            }
            else -> true
        }
    }

    private fun registerDoctor() {
        if (validateRegisterDetails()) {
            showProgressDialog("Proszę czekać...")

            val email: String = inputEmail?.text.toString().trim { it <= ' ' }
            val password: String = inputPassword?.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->

                        if (task.isSuccessful) {
                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val doctor = Doctor(
                                id = firebaseUser.uid,
                                firstName = inputFirstName?.text.toString().trim { it <= ' ' },
                                lastName = inputLastName?.text.toString().trim { it <= ' ' },
                                phoneNumber = inputPhoneNumber?.text.toString().trim { it <= ' ' },
                                address = inputAddress?.text.toString().trim { it <= ' ' },
                                title = inputTitle?.text.toString().trim { it <= ' ' },
                                imageId = "default_image_id",
                                patientIds = listOf()  // Empty list for patient IDs initially
                            )

                            // Save doctor details to Firestore
                            DoctorFirestoreDatabaseOperations().registerDoctor(this, doctor)

                            // Save login details to the 'login' collection
                            val loginDetails = hashMapOf(
                                "email" to email,
                                "password" to password,
                                "role" to "lekarz"
                            )

                            FirebaseFirestore.getInstance().collection("login")
                                .document(firebaseUser.uid)
                                .set(loginDetails)
                                .addOnSuccessListener {
                                    userRegistrationSuccess()
                                }
                                .addOnFailureListener { e ->
                                    hideProgressDialog()
                                    showErrorSnackBar("Błąd rejestracji: ${e.message}", true)
                                }

                        } else {
                            hideProgressDialog()
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    })
        }
    }

    fun userRegistrationSuccess() {
        hideProgressDialog()
        Toast.makeText(this@RegisterActivity, "Rejestracja zakończona sukcesem.", Toast.LENGTH_LONG).show()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}
