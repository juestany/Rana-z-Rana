package com.example.sr13.doctor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sr13.R
import com.example.sr13.doctor.check_patient_rv.SubmittedReportsAdapter
import com.example.sr13.doctor.check_patient_rv.SubmittedReportsViewModel
import com.example.sr13.firestore.Doctor
import com.example.sr13.firestore.Patient
import com.example.sr13.firestore.Report
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DoctorCheckPatientActivity : AppCompatActivity() {

    private lateinit var removePatientBtn: Button
    private lateinit var firestore: FirebaseFirestore
    private lateinit var patientNameMain: TextView
    private lateinit var patientProfilePic: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var reportsAdapter: SubmittedReportsAdapter
    private lateinit var goToChatBtn: Button
    private val reportsList = mutableListOf<SubmittedReportsViewModel>()

    private var patientId: String? = null
    private var doctorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_check_patient)

        patientNameMain = findViewById(R.id.patientNameMain)
        patientProfilePic = findViewById(R.id.patientProfilePicMain)
        removePatientBtn = findViewById(R.id.removePatientBtn)
        goToChatBtn = findViewById(R.id.patientChatBtn)

        recyclerView = findViewById(R.id.patientSubmittedReportsRecyclerView)
        firestore = FirebaseFirestore.getInstance()

        reportsAdapter = SubmittedReportsAdapter(reportsList) { reportId ->
            val intent = Intent(this, DoctorCheckPatientReportActivity::class.java)
            intent.putExtra("REPORT_ID", reportId)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = reportsAdapter

        patientId = intent.getStringExtra("PATIENT_ID")
        doctorId = FirebaseAuth.getInstance().currentUser?.uid

        patientId?.let {
            loadPatientData(it)
            loadPatientReports(it)
        }

        removePatientBtn.setOnClickListener {
            patientId?.let { id ->
                removePatientFromDatabase(id)
            }
        }

        goToChatBtn.setOnClickListener {
            checkIfChatRoomExists()
        }
    }

    private fun checkIfChatRoomExists() {
        val chatsRef = FirebaseFirestore.getInstance().collection("chats")
        doctorId?.let {
            chatsRef.whereArrayContains("participants", it)
                .get()
                .addOnSuccessListener { documents ->
                    var chatRoomExists = false
                    for (document in documents) {
                        val participants = document.get("participants") as List<*>
                        if (participants.contains(patientId)) {
                            // Chat room already exists, open this conversation
                            chatRoomExists = true
                            val chatRoomId = document.id
                            // Open com.example.sr13.doctor.ChatActivity with this chatRoomId
                            openChat(chatRoomId)
                            break
                        }
                    }
                    if (!chatRoomExists) {
                        // No existing chat room, create a new one
                        patientId?.let { it1 -> createChatRoom(doctorId!!, it1) }
                    }
                }
        }
    }

    private fun openChat(chatRoomId: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("CHAT_ROOM_ID", chatRoomId) // Pass the chatRoomId to the com.example.sr13.doctor.ChatActivity
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
            // Navigate to the chat screen with chatRoomId
            openChat(chatRoomId)
        }
    }

    private fun loadPatientData(patientId: String) {
        firestore.collection("patient")
            .document(patientId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val patient = document.toObject(Patient::class.java)
                    val imageUrl = document.getString("imageId")
                    if (patient != null) {
                        val fullName = "${patient.firstName} ${patient.lastName}"
                        patientNameMain.text = fullName
                        imageUrl?.let {
                            fetchImageFromFirebaseStorage(it)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors
            }
    }

    private fun fetchImageFromFirebaseStorage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(patientProfilePic)
    }

    private fun removePatientFromDatabase(patientId: String) {
        // Remove the patient document
        firestore.collection("patient")
            .document(patientId)
            .delete()
            .addOnSuccessListener {
                // Successfully deleted the patient
                removePatientFromDoctor(patientId)
            }
            .addOnFailureListener { e ->
                // Handle any errors
            }
    }

    private fun removePatientFromDoctor(patientId: String) {
        doctorId?.let { docId ->
            val doctorRef = firestore.collection("doctor").document(docId)

            firestore.runTransaction { transaction ->
                val doctorSnapshot = transaction.get(doctorRef)
                val doctor = doctorSnapshot.toObject(Doctor::class.java)
                doctor?.let {
                    val updatedPatientIds = it.patientIds.toMutableList().apply {
                        remove(patientId)
                    }
                    transaction.update(doctorRef, "patientIds", updatedPatientIds)
                }
            }.addOnSuccessListener {
                // Successfully removed the patient from the doctor's list
                val intent = Intent(this@DoctorCheckPatientActivity, DoctorMyPatientsActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener { e ->
                // Handle any errors
            }
        }
    }

    private fun loadPatientReports(patientId: String) {
        firestore.collection("reports")
            .whereEqualTo("userId", patientId)
            .get()
            .addOnSuccessListener { documents ->
                reportsList.clear() // Clear the list before adding new data
                for (document in documents) {
                    val report = document.toObject(Report::class.java)
                    // Fetch patient details
                    firestore.collection("patient")
                        .document(report.userId)
                        .get()
                        .addOnSuccessListener { patientDocument ->
                            if (patientDocument.exists()) {
                                val patient = patientDocument.toObject(Patient::class.java)
                                val fullName = "${patient?.firstName} ${patient?.lastName}"
                                val reportModel = SubmittedReportsViewModel(
                                    R.drawable.ic_paper_icon,
                                    fullName,
                                    report.date,
                                    report.reportId
                                )
                                reportsList.add(reportModel)

                                // Sort the list by date before updating the adapter
                                val sortedReportsList = reportsList.sortedByDescending { it.getFormattedDate() }

                                // Update the adapter with the sorted list
                                reportsAdapter.updateList(sortedReportsList)
                            }
                        }
                        .addOnFailureListener { e ->
                            // Handle any errors while fetching patient details
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors while fetching reports
            }
    }
}
