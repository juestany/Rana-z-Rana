package com.example.sr13.patient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.sr13.LoginActivity
import com.example.sr13.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class PatientMainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var patientNameMain: TextView
    private lateinit var patientRoleMain: TextView
    private lateinit var patientProfilePicMain: ImageFilterView
    private lateinit var patientSubmitReportBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var logoutBtn: Button
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private var doctorLocation: LatLng? = null
    private var patientAddress: String? = null
    private var useDatabaseAddress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_main)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        patientNameMain = findViewById(R.id.patientNameMain)
        patientRoleMain = findViewById(R.id.doctorRoleMain)
        patientProfilePicMain = findViewById(R.id.patientProfilePicMain)
        patientSubmitReportBtn = findViewById(R.id.addReportBtn)
        logoutBtn = findViewById(R.id.logoutBtn)

        getPatientData()

        patientSubmitReportBtn.setOnClickListener {
            val intent = Intent(this@PatientMainActivity, PatientAddReportActivity::class.java)
            startActivity(intent)
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this@PatientMainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.znajdzLekarza).setOnClickListener {
            if (currentLocation != null) {
                calculateRouteToDoctor(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
            } else if (useDatabaseAddress && patientAddress != null) {
                geocodeAddressAndCalculateRoute(patientAddress!!)
            }
        }

        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.lastLocation
                currentLocation?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getPatientAddressFromDatabase()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun getPatientData() {
        val userId = auth.currentUser?.uid
        Log.e("SPRAWDZENIE", "ID UZYTKOWNIKA: ${userId}")

        userId?.let { uid ->
            firestore.collection("patient")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")
                        val imageUrl = document.getString("imageId")
                        patientAddress = document.getString("adress")
                        val doctorId = document.getString("doctorId")
                        Log.e("SPRAWDZENIE 2", "ADRES UYZTKOWNIKA: ${patientAddress}")


                        patientNameMain.text = "$firstName $lastName"
                        patientRoleMain.text = "Pacjent"

                        imageUrl?.let {
                            fetchImageFromFirebaseStorage(it)
                        }

                        doctorId?.let {
                            getDoctorLocation(it)
                        }
                    } else {
                        Log.e("PatientData", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PatientData", "Error fetching patient data", exception)
                }
        }
    }

    private fun getPatientAddressFromDatabase() {
        useDatabaseAddress = true
        patientAddress?.let {
            Log.d("PatientAddress", "Using patient address from database: $it")
        }
    }

    private fun fetchImageFromFirebaseStorage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .into(patientProfilePicMain)
    }

    private fun getDoctorLocation(doctorId: String) {
        firestore.collection("doctor").document(doctorId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val lat = document.getDouble("lat")
                    val lng = document.getDouble("lng")
                    if (lat != null && lng != null) {
                        doctorLocation = LatLng(lat, lng)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching doctor location", e)
            }
    }

    private fun geocodeAddressAndCalculateRoute(address: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocationName(address, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val location = addresses[0]
            val patientLatLng = LatLng(location.latitude, location.longitude)
            calculateRouteToDoctor(patientLatLng)
        }
    }

    private fun calculateRouteToDoctor(currentLatLng: LatLng) {
        // Używamy lokalizacji GPS pacjenta do wyznaczenia trasy
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${currentLatLng.latitude},${currentLatLng.longitude}&destination=${doctorLocation?.latitude},${doctorLocation?.longitude}&travelmode=driving")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        currentLocation?.let {
            val currentLatLng = LatLng(it.latitude, it.longitude)
            mMap.addMarker(MarkerOptions().position(currentLatLng).title("You are here"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates()
        }
    }
}
