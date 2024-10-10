package com.example.sr13.firestore

data class Message(
    val senderId: String = "",
    val message: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)