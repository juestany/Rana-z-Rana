package com.example.sr13.firestore

import com.google.firebase.firestore.PropertyName

data class Report(
    @PropertyName("id")
    var id: String="",

    @PropertyName("userId")
    var userId: String="",

    @PropertyName("imageId")
    var imageiD: String="",

    @PropertyName("comment")
    var comment: String=""
)
