package com.example.sr13.doctor

import CustomAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sr13.MyPatientsViewModel
import com.example.sr13.R

class DoctorMyPatientsActivity : AppCompatActivity() {

    private lateinit var addPatientBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.doctor_my_patients)

        addPatientBtn = findViewById(R.id.addPatientBtn)

        val recyclerview = findViewById<RecyclerView>(R.id.myPatientsRecyclerView)
        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)
        // ArrayList of class ItemsViewModel
        val data = ArrayList<MyPatientsViewModel>()

        // This loop will create 20 Views containing
        // the image with the count of view
        for (i in 1..20) {
            data.add(MyPatientsViewModel(R.drawable.ic_profile_icon, "Item " + i))
        }

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(data)

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter

        addPatientBtn.setOnClickListener() {
            val intent = Intent(this@DoctorMyPatientsActivity, DoctorAddPatientActivity::class.java)
            startActivity(intent)
        }
    }

    // TODO: Recycler View with patients from the database
}