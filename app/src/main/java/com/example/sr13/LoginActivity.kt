package com.example.sr13

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sr13.doctor.DoctorMainActivity
import com.example.sr13.patient.PatientMainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class LoginActivity : BaseActivity(), View.OnClickListener {
    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var loginButton: Button? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val ALARM_PERMISSION_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        inputEmail = findViewById(R.id.loginEmail)
        inputPassword = findViewById(R.id.loginPassword)
        loginButton = findViewById(R.id.loginButton)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance() // Dodano inicjalizację zmiennej auth
        loginButton?.setOnClickListener {
            logInRegisteredUser()
        }
        createNotificationChannel()
        if (checkAlarmPermission()) {
            setDailyReminder()
        } else {
            requestAlarmPermission()
        }
    }

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
                        Log.d(TAG, "User logged in successfully")
                        goToNextActivity()
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

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
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("reminderChannel", "Reminder Channel", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Kanał przypomnień"
            }
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Sprawdza, czy aplikacja ma uprawnienie do ustawienia alarmu
    fun checkAlarmPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.SET_ALARM)
        Log.d(TAG, "SET_ALARM permission: $permission")
        return permission == PackageManager.PERMISSION_GRANTED
    }


    // Żąda uprawnienia od użytkownika, jeśli go brakuje
    private fun requestAlarmPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SET_ALARM), ALARM_PERMISSION_REQUEST_CODE)
    }

    // Obsługuje odpowiedź użytkownika na prośbę o uprawnienie
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ALARM_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setDailyReminder()
            } else {
                Toast.makeText(this, "Uprawnienie SET_ALARM jest wymagane", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Ustawia codzienny alarm przypomnienia
    private fun setDailyReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val calendar: Calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9) // Ustawiamy godzinę powiadomienia na 9:00
            set(Calendar.MINUTE, 8)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
