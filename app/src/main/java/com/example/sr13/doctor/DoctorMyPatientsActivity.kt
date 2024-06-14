package com.example.sr13.doctor

import com.example.sr13.doctor.my_patients_recyclerview.MyPatientsAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sr13.doctor.my_patients_recyclerview.MyPatientsViewModel
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

        // Mock data for patients' names
        val patientsNames = listOf(
            // TODO: get from database
            "Julia Nowak", "Grzegorz Małysz", "Leokadia Rafałowicz",
            "Krzysztof Kowalski", "Anna Dąbrowska", "Michał Wiśniewski"
        )

        // ArrayList of class ItemsViewModel
        val data = ArrayList<MyPatientsViewModel>()

        // Populate the data with mock patient names
        for (name in patientsNames) {
            data.add(MyPatientsViewModel(R.drawable.ic_profile_icon, name))
        }

        // This will pass the ArrayList to our Adapter
        val adapter = MyPatientsAdapter(data)

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter

        addPatientBtn.setOnClickListener {
            val intent = Intent(this@DoctorMyPatientsActivity, DoctorAddPatientActivity::class.java)
            startActivity(intent)
        }
    }
}
