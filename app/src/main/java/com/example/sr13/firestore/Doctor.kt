package com.example.sr13.firestore

import com.google.firebase.firestore.PropertyName

data class Doctor(
    @PropertyName("id")
    var id: String="",

    @PropertyName("firstName")
    var firstName: String="",

    @PropertyName("lastName")
    var lastName: String="",

    @PropertyName("phoneNumber")
    var phoneNumber: String="",

    @PropertyName("adress")
    var adress: String="",

    @PropertyName("title")
    var title: String=""
)
