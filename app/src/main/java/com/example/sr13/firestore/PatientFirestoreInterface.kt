package com.example.sr13.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot

interface PatientFirestoreInterface {
    fun addPatient(patient: Patient): Task<Void>
    fun getPatientById(id: String): Task<QuerySnapshot>
    fun updatePatient(id: String, patient: Patient): Task<Void>
    fun deletePatient(id: String): Task<Void>
}
