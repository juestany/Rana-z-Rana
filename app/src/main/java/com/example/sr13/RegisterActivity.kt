package com.example.sr13

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.sr13.firestore.Doctor
import com.example.sr13.firestore.DoctorFirestoreDatabaseOperations
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

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

    /**
     * Method to validate registration details.
     * @return True if the details are valid, False otherwise.
     */
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

    /**
     * Method to register a new doctor.
     */
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
                                firebaseUser.uid,
                                inputFirstName?.text.toString().trim { it <= ' ' },
                                inputLastName?.text.toString().trim { it <= ' ' },
                                inputPhoneNumber?.text.toString().trim { it <= ' ' },
                                inputAddress?.text.toString().trim { it <= ' ' },
                                inputTitle?.text.toString().trim { it <= ' ' }
                            )

                            DoctorFirestoreDatabaseOperations().registerDoctor(this, doctor)
                        } else {
                            hideProgressDialog()
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    })
        }
    }

    /**
     * Method called after successful registration.
     */
    fun userRegistrationSuccess() {
        Toast.makeText(this@RegisterActivity, "Rejestracja zakończona sukcesem.", Toast.LENGTH_LONG).show()
    }
}
