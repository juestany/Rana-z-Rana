package com.example.sr13

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.sr13.firestore.FirestoreDatabaseOperations
import com.example.sr13.firestore.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : BaseActivity() {

    private var registerButton: Button? = null
    private var inputEmail: EditText? = null
    private var inputName: EditText? = null
    private var inputPassword: EditText? = null
    private var inputRepPass: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        // Inicjalizacja pól wejściowych i przycisku rejestracji
        registerButton = findViewById(R.id.registerButton)
        inputEmail = findViewById(R.id.registerEmail)
        inputName = findViewById(R.id.registerName)
        inputPassword = findViewById(R.id.registerPassword)
        inputRepPass = findViewById(R.id.registerPassword2Repeat)

        // Ustawienie nasłuchiwania kliknięć przycisku rejestracji
        registerButton?.setOnClickListener{
            registerUser()
        }
    }

    /**
     * Metoda walidująca wprowadzone dane rejestracji.
     * @return True, jeśli dane są poprawne, w przeciwnym razie False.
     */
    private fun validateRegisterDetails(): Boolean {

        return when{
            TextUtils.isEmpty(inputEmail?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email),true)
                false
            }
            TextUtils.isEmpty(inputName?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_name),true)
                false
            }
            TextUtils.isEmpty(inputPassword?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password),true)
                false
            }
            TextUtils.isEmpty(inputRepPass?.text.toString().trim{ it <= ' '}) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_reppassword),true)
                false
            }
            inputPassword?.text.toString().trim {it <= ' '} != inputRepPass?.text.toString().trim{it <= ' '} -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_mismatch),true)
                false
            }
            else -> true
        }
    }

    /**
     * Metoda przechodzenia do aktywności logowania.
     */
    fun goToLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // finish(), po to aby użytkownik nie mógł już wrócić do aktualnej aktywności bez restartowania aplikacji
    }

    /**
     * Metoda rejestracji użytkownika za pomocą Firebase Authentication.
     */
    private fun registerUser(){
        if (validateRegisterDetails()){
            val login: String = inputEmail?.text.toString().trim() {it <= ' '}
            val password: String = inputPassword?.text.toString().trim() {it <= ' '}
            val name: String = inputName?.text.toString().trim() {it <= ' '}

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(login,password).addOnCompleteListener(
                OnCompleteListener <AuthResult>{ task ->
                    if(task.isSuccessful){
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        showErrorSnackBar("You are registered successfully. Your user id is ${firebaseUser.uid}",false)

                        val user = User("Testowe ID",
                            name,
                            true,
                            login,
                        )
                        FirestoreDatabaseOperations().registerUserFS(this@RegisterActivity, user)

                        FirebaseAuth.getInstance().signOut()
                        finish()

                    } else{
                        showErrorSnackBar(task.exception!!.message.toString(),true)
                    }
                }
            )
        }
    }

    /**
     * Metoda wywoływana po udanej rejestracji użytkownika wyświetlająca wiadomość Toast.
     */
    fun  userRegistrationSuccess(){
        Toast.makeText(this@RegisterActivity, resources.getString(R.string.register_success), Toast.LENGTH_LONG).show()
    }
}