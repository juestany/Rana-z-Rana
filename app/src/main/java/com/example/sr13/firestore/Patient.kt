package com.example.sr13.firestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Patient(
    @PropertyName("id")
    var id: String="",

    @PropertyName("imageId")
    var imageId: String="",

    @PropertyName("firstName")
    var firstName: String="",

    @PropertyName("lastName")
    var lastName: String="",

    @PropertyName("pesel")
    var pesel: String="",

    @PropertyName("phoneNumber")
    var phoneNumber: String="",

    @PropertyName("adress")
    var adress: String ="",

    @PropertyName("birthDate")
    var birthDate: Timestamp? = Timestamp.now(),

    @PropertyName("operation")
    var operation: String = "",

    @PropertyName("doctorId")
    var doctorId: String=""
) {
}
