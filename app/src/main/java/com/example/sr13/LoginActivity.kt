package com.example.sr13

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity(), View.OnClickListener {
        private var inputEmail: EditText? = null
        private var inputPassword: EditText? = null
        private var loginButton: Button? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.login)

            inputEmail = findViewById(R.id.loginEmail)
            inputPassword = findViewById(R.id.loginPassword)
            loginButton = findViewById(R.id.loginButton)

            loginButton?.setOnClickListener {
                logInRegisteredUser()
            }
        }


        override fun onClick(view: View?) {
            if (view != null) {
                when (view.id) {
                    R.id.registerUserButton -> {
                        val intent = Intent(this, RegisterActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

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

        private fun logInRegisteredUser() {
            if (validateLoginDetails()) {
                val email = inputEmail?.text.toString().trim() { it <= ' ' }
                val password = inputPassword?.text.toString().trim() { it <= ' ' }
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showErrorSnackBar(resources.getString(R.string.login_successfull), false)
                            finish()
                        } else {
                            showErrorSnackBar(task.exception!!.message.toString(), true)
                        }
                    }
            }
        }
    }