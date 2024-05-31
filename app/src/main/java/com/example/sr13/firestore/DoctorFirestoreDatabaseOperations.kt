package com.example.sr13.firestore

import com.example.sr13.RegisterActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class DoctorFirestoreDatabaseOperations : DoctorFirestoreInterface {
    private val db = FirebaseFirestore.getInstance()
    private val doctorsCollection = db.collection("doctor")

    override fun addDoctor(doctor: Doctor): Task<Void> {
        return doctorsCollection.document(doctor.id).set(doctor)
    }

    override fun getDoctorById(id: String): Task<QuerySnapshot> {
        return doctorsCollection.whereEqualTo("id", id).get()
    }

    override fun updateDoctor(id: String, doctor: Doctor): Task<Void> {
        return doctorsCollection.document(id).set(doctor)
    }

    override fun deleteDoctor(id: String): Task<Void> {
        return doctorsCollection.document(id).delete()
    }

    fun registerDoctor(activity: RegisterActivity, doctor: Doctor) {
        addDoctor(doctor).addOnSuccessListener {
            activity.userRegistrationSuccess()
            activity.hideProgressDialog()
        }.addOnFailureListener { e ->
            activity.hideProgressDialog()
            activity.showErrorSnackBar(e.message.toString(), true)
        }
    }
}
