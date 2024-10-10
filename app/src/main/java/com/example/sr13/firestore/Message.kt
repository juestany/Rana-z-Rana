package com.example.sr13.firestore

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null // Ensure this is the correct type
)