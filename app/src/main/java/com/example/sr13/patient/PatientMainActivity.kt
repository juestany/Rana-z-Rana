package com.example.sr13.patient

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
import com.example.sr13.doctor.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PatientMainActivity : AppCompatActivity() {

    private lateinit var patientNameMain: TextView
    private lateinit var patientRoleMain: TextView
    private lateinit var patientProfilePicMain: ImageFilterView
    private lateinit var patientSubmitReportBtn: Button
    private lateinit var goToChatBtn: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var logoutBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_main)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        patientNameMain = findViewById(R.id.patientNameMain)
        patientRoleMain = findViewById(R.id.doctorRoleMain)
        patientProfilePicMain = findViewById(R.id.patientProfilePicMain)
        patientSubmitReportBtn = findViewById(R.id.addReportBtn)
        goToChatBtn = findViewById(R.id.button2)
        logoutBtn = findViewById(R.id.logoutBtn)

        // Set up initial values or listeners here
        getPatientData()

        // Set a click listener for the button
        patientSubmitReportBtn.setOnClickListener {
            val intent = Intent(this@PatientMainActivity, PatientAddReportActivity::class.java)
            startActivity(intent)

        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this@PatientMainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        goToChatBtn.setOnClickListener {
            checkIfChatRoomExists()
        }
    }

    private fun openChat(chatRoomId: String) {
        val intent = Intent(this@PatientMainActivity, ChatActivity::class.java)
        intent.putExtra("CHAT_ROOM_ID", chatRoomId)
        startActivity(intent)
    }

    private fun createChatRoom(doctorId: String, patientId: String) {
        val chatRoomData = hashMapOf(
            "participants" to listOf(doctorId, patientId),
            "lastMessage" to "",
            "lastUpdated" to FieldValue.serverTimestamp()
        )

        val chatsRef = FirebaseFirestore.getInstance().collection("chats")
        chatsRef.add(chatRoomData).addOnSuccessListener { documentReference ->
            val chatRoomId = documentReference.id
            openChat(chatRoomId)
        }
    }

    private fun checkIfChatRoomExists() {
        val chatsRef = FirebaseFirestore.getInstance().collection("chats")
        val patientId = FirebaseAuth.getInstance().currentUser?.uid

        // Assuming there's a field in the patient collection to identify the doctor
        firestore.collection("patient").document(patientId!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val doctorId = documentSnapshot.getString("doctorId") ?: return@addOnSuccessListener

                chatsRef.whereArrayContains("participants", patientId)
                    .get()
                    .addOnSuccessListener { documents ->
                        var chatRoomExists = false
                        for (document in documents) {
                            val participants = document.get("participants") as List<*>
                            if (participants.contains(doctorId)) {
                                // Chat room exists, open this conversation
                                chatRoomExists = true
                                val chatRoomId = document.id
                                openChat(chatRoomId)
                                break
                            }
                        }
                        if (!chatRoomExists) {
                            // No existing chat room, create a new one
                            createChatRoom(doctorId, patientId)
                        }
                    }
            }
    }

    private fun getPatientData() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = auth.currentUser?.uid

        userId?.let { uid ->
            firestore.collection("patient")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        val phoneNumber = document.getString("phoneNumber")
                        val imageUrl = document.getString("imageId")

                        patientNameMain.text = "$firstName $lastName"
                        patientRoleMain.text = "Pacjent"

                        Log.d("PatientData", "Fetched imageUrl: $imageUrl")
                        imageUrl?.let {
                            fetchImageFromFirebaseStorage(it)
                        }
                    } else {
                        Log.e("PatientData", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PatientData", "Error fetching patient data", exception)
                }
        }
    }

    private fun fetchImageFromFirebaseStorage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(patientProfilePicMain)
    }
}
