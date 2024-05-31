package com.example.sr13.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class PatientFirestoreDatabaseOperations : PatientFirestoreInterface {
    private val db = FirebaseFirestore.getInstance()
    private val patientsCollection = db.collection("patient")

    override fun addPatient(patient: Patient): Task<Void> {
        return patientsCollection.document(patient.id).set(patient)
    }

    override fun getPatientById(id: String): Task<QuerySnapshot> {
        return patientsCollection.whereEqualTo("id", id).get()
    }

    override fun updatePatient(id: String, patient: Patient): Task<Void> {
        return patientsCollection.document(id).set(patient)
    }

    override fun deletePatient(id: String): Task<Void> {
        return patientsCollection.document(id).delete()
    }
}
