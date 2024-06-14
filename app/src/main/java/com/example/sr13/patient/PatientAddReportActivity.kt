package com.example.sr13.patient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.example.sr13.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientAddReportActivity : AppCompatActivity(){

    private lateinit var previewImage: AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_add_report)

        // Adding the current date to the report
        findViewById<TextView>(R.id.patientReportDate).text = getCurrentDate()

        // TODO: update the name of the operation
        //findViewById<TextView>(R.id.patientOperationDesc).text =

        previewImage = findViewById(R.id.previewImage)

        // Wait for adding an image
        val uploadImageBtn = findViewById<Button>(R.id.uploadImageBtn)
        uploadImageBtn.setOnClickListener {
            // TODO: handle adding an image
            val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            changeImage.launch(pickImg)
        }

        // Wait for submit button
        val patientSubmitReportBtn = findViewById<Button>(R.id.patientSubmitReportBtn)
        patientSubmitReportBtn.setOnClickListener {
            // TODO: save everything to database in a report. fetch patient's comment
            val intent = Intent(this, PatientMainActivity::class.java)
            startActivity(intent)
        }
    }

    private val changeImage = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { it: ActivityResult ->
        if (it.resultCode == Activity.RESULT_OK) {
            val data = it.data
            val imgUri = data?.data
            previewImage.setImageURI(imgUri)
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }
}