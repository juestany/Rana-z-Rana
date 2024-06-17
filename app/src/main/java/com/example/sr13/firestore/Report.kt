package com.example.sr13.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Report(
    @PropertyName("reportId")
    var reportId: String="",

    @PropertyName("userId")
    var userId: String="",

    @PropertyName("imageUrl")
    var imageUrl: String="",

    @PropertyName("comment")
    var comment: String="",

    @PropertyName("date")
    var date: String=""
)
