package com.example.sr13.doctor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.bumptech.glide.Glide
import com.example.sr13.LoginActivity
import com.example.sr13.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctorMainActivity : AppCompatActivity() {

    //TODO: better looking layout
    //TODO: today's reports recycler view
    // TODO: log out button

    private lateinit var doctorNameMain: TextView
    private lateinit var doctorRoleMain: TextView
    private lateinit var doctorProfilePicMain: ImageFilterView
    private lateinit var doctorSubmittedReportsRecyclerView: RecyclerView
    private lateinit var myPatientsBtn: Button
    private lateinit var logoutBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_main)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        doctorNameMain = findViewById(R.id.patientNameMain)
        doctorRoleMain = findViewById(R.id.doctorRoleMain)
        doctorProfilePicMain = findViewById(R.id.patientProfilePicMain)
        doctorSubmittedReportsRecyclerView = findViewById(R.id.doctorTodaySubmittedReportsRecyclerView)
        myPatientsBtn = findViewById(R.id.myPatientsBtn)
        logoutBtn = findViewById(R.id.logoutBtn)

        // Set up initial values or listeners here
        getDoctorData()

        myPatientsBtn.setOnClickListener() {
            val intent = Intent(this@DoctorMainActivity, DoctorMyPatientsActivity::class.java)
            startActivity(intent)
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this@DoctorMainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getDoctorData() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = auth.currentUser?.uid

        userId?.let { uid ->
            firestore.collection("doctor")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        val title = document.getString("title")
                        val address = document.getString("adress") // Note: fixed typo to "address"
                        val phoneNumber = document.getString("phoneNumber")
                        val imageUrl = document.getString("imageId")

                        doctorNameMain.text = "$firstName $lastName"
                        doctorRoleMain.text = title

                        Log.d("DoctorData", "Fetched imageUrl: $imageUrl")

                        // Fetch and display the image from Firebase Storage using URL
                        imageUrl?.let {
                            fetchImageFromFirebaseStorage(it)
                        }
                    } else {
                        Log.e("DoctorData", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("DoctorData", "Error fetching doctor data", exception)
                }
        }
    }

    private fun fetchImageFromFirebaseStorage(imageUrl: String) {
        // Load the image into ImageFilterView using Glide
        Glide.with(this)
            .load(imageUrl)
            .into(doctorProfilePicMain)
    }
}
