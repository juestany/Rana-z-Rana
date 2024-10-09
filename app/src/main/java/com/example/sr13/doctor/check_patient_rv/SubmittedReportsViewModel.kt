package com.example.sr13.doctor.check_patient_rv

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SubmittedReportsViewModel(
    val image: Int,
    val patientId: String,
    val reportDate: String,
    val reportId: String
) {
    // Function to convert reportDate String ("dd/MM/yyyy") into Date object
    fun getFormattedDate(): Date? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            dateFormat.parse(reportDate)
        } catch (e: Exception) {
            null
        }
    }
}
