package com.example.sr13.doctor.check_patient_rv

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data model class for representing a submitted report.
 *
 * @property image The resource ID of the image associated with the report.
 * @property patientId The ID of the patient who submitted the report.
 * @property reportDate The date of the report in String format ("dd/MM/yyyy").
 * @property reportId The unique ID of the report.
 */
class SubmittedReportsViewModel(
    val image: Int,
    val patientId: String,
    val reportDate: String,
    val reportId: String
) {
    /**
     * Converts the `reportDate` String (formatted as "dd/MM/yyyy") into a Date object.
     *
     * @return The parsed Date object if parsing succeeds, or null if an error occurs.
     */
    fun getFormattedDate(): Date? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            dateFormat.parse(reportDate)
        } catch (e: Exception) {
            // Return null if the date format is invalid or parsing fails.
            null
        }
    }
}