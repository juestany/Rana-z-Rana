package com.example.sr13.firestore

import com.google.firebase.firestore.PropertyName

data class Doctor(
    @PropertyName("id")
    var id: String = "",

    @PropertyName("imageId")
    var imageId: String = "",

    @PropertyName("firstName")
    var firstName: String = "",

    @PropertyName("lastName")
    var lastName: String = "",

    @PropertyName("phoneNumber")
    var phoneNumber: String = "",

    @PropertyName("address")
    var address: String = "",

    @PropertyName("title")
    var title: String = "",

    @PropertyName("patientIds")
    var patientIds: List<String> = mutableListOf()
)
