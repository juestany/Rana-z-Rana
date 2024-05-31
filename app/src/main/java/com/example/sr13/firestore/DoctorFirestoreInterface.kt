package com.example.sr13.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

interface DoctorFirestoreInterface {
    fun addDoctor(doctor: Doctor): Task<Void>
    fun getDoctorById(id: String): Task<QuerySnapshot>
    fun updateDoctor(id: String, doctor: Doctor): Task<Void>
    fun deleteDoctor(id: String): Task<Void>
}
