package com.example.sr13.patient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.sr13.LoginActivity
import com.example.sr13.R
import com.example.sr13.doctor.ChatActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class PatientMainActivity : AppCompatActivity(), OnMapReadyCallback {

    // UI Components
    private lateinit var patientNameMain: TextView
    private lateinit var patientRoleMain: TextView
    private lateinit var patientProfilePicMain: ImageFilterView
    private lateinit var patientSubmitReportBtn: Button
    private lateinit var goToChatBtn: Button
    private lateinit var logoutBtn: Button
    private lateinit var rangeSeekBar: SeekBar
    private lateinit var seekBarValue: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var currentLocation: Location? = null
    private var doctorLocation: LatLng? = null
    private var patientAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.patient_main)

        // Initialize Firebase and Location components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize UI components
        patientNameMain = findViewById(R.id.patientNameMain)
        patientRoleMain = findViewById(R.id.doctorRoleMain)
        patientProfilePicMain = findViewById(R.id.patientProfilePicMain)
        patientSubmitReportBtn = findViewById(R.id.addReportBtn)
        goToChatBtn = findViewById(R.id.button2)
        logoutBtn = findViewById(R.id.logoutBtn)
        rangeSeekBar = findViewById(R.id.rangeSeekBar)
        seekBarValue = findViewById(R.id.seekBarValue)

        // Initialize Places API
        Places.initialize(applicationContext, "YOUR_API_KEY")

        // Load patient data and set up listeners
        getPatientData()
        setupListeners()

        // Setup map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestLocationUpdates()
    }

    private fun setupListeners() {
        patientSubmitReportBtn.setOnClickListener {
            startActivity(Intent(this, PatientAddReportActivity::class.java))
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        goToChatBtn.setOnClickListener { checkIfChatRoomExists() }

        findViewById<Button>(R.id.znajdzLekarza).setOnClickListener {
            if (currentLocation != null) {
                calculateRouteToDoctor(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
            } else if (patientAddress != null) {
                geocodeAddressAndCalculateRoute(patientAddress!!)
            }
        }

        findViewById<Button>(R.id.znajdzApteke).setOnClickListener {
            val radius = rangeSeekBar.progress * 1000
            findNearbyPlaces("pharmacy", radius)
        }

        findViewById<Button>(R.id.znajdzSzpital).setOnClickListener {
            val radius = rangeSeekBar.progress * 1000
            findNearbyPlaces("hospital", radius)
        }

        rangeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarValue.text = "${progress} kilometrów"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun getPatientData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("patient").document(userId).get().addOnSuccessListener { document ->
            document?.let {
                val firstName = it.getString("firstName") ?: ""
                val lastName = it.getString("lastName") ?: ""
                val imageUrl = it.getString("imageId")
                patientAddress = it.getString("adress")
                val doctorId = it.getString("doctorId")
                patientNameMain.text = "$firstName $lastName"
                patientRoleMain.text = "Pacjent"

                imageUrl?.let { url -> Glide.with(this).load(url).into(patientProfilePicMain) }
                doctorId?.let { getDoctorLocation(it) }
            }
        }.addOnFailureListener { e ->
            Log.e("PatientData", "Failed to fetch patient data", e)
        }
    }

    private fun checkIfChatRoomExists() {
        val chatsRef = firestore.collection("chats")
        val patientId = auth.currentUser?.uid

        patientId?.let { id ->
            firestore.collection("patient").document(id).get().addOnSuccessListener { document ->
                val doctorId = document.getString("doctorId") ?: return@addOnSuccessListener

                chatsRef.whereArrayContains("participants", id).get().addOnSuccessListener { chatDocuments ->
                    var chatRoomExists = false
                    for (doc in chatDocuments) {
                        val participants = doc.get("participants") as List<*>
                        if (participants.contains(doctorId)) {
                            chatRoomExists = true
                            val chatRoomId = doc.id
                            fetchDoctorName(doctorId) { fullName ->
                                openChat(chatRoomId, fullName) // Open the existing chat room
                            }
                            break
                        }
                    }
                    if (!chatRoomExists) {
                        // If no chat room exists, create a new one
                        createChatRoom(doctorId, id)
                    }
                }.addOnFailureListener { e ->
                    Log.e("ChatRoom", "Error fetching chat rooms", e)
                }
            }.addOnFailureListener { e ->
                Log.e("PatientData", "Error fetching patient data", e)
            }
        }
    }
    private fun fetchDoctorName(doctorId: String, callback: (String) -> Unit) {
        firestore.collection("doctor").document(doctorId).get().addOnSuccessListener { document ->
            val firstName = document.getString("firstName") ?: ""
            val lastName = document.getString("lastName") ?: ""
            callback("$firstName $lastName")
        }.addOnFailureListener { e ->
            Log.e("DoctorData", "Error fetching doctor data", e)
            callback("Unknown Doctor")
        }
    }

    private fun createChatRoom(doctorId: String, patientId: String) {
        val chatRoomData = hashMapOf(
            "participants" to listOf(doctorId, patientId),
            "lastMessage" to "",
            "lastUpdated" to FieldValue.serverTimestamp()
        )
        firestore.collection("chats").add(chatRoomData).addOnSuccessListener { document ->
            val chatRoomId = document.id
            fetchDoctorName(doctorId) { fullName ->
                openChat(chatRoomId, fullName) // Open the newly created chat room
            }
        }.addOnFailureListener { e ->
            Log.e("ChatRoom", "Error creating chat room", e)
        }
    }

    private fun openChat(chatRoomId: String, participantName: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("CHAT_ROOM_ID", chatRoomId)
            putExtra("PARTICIPANT_NAME", participantName)
        }
        startActivity(intent)
    }


    private fun getDoctorLocation(doctorId: String) {
        firestore.collection("doctor").document(doctorId).get().addOnSuccessListener { document ->
            document?.getString("adress")?.let { address ->
                geocodeDoctorAddress(address)
            }
        }
    }

    private fun geocodeDoctorAddress(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val geocoder = Geocoder(this@PatientMainActivity, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)
            addresses?.firstOrNull()?.let {
                doctorLocation = LatLng(it.latitude, it.longitude)
            }
        }
    }



    private fun calculateRouteToDoctor(currentLatLng: LatLng) {
        doctorLocation?.let {
            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${currentLatLng.latitude},${currentLatLng.longitude}&destination=${it.latitude},${it.longitude}&travelmode=driving")
            val intent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }
    }

    private fun geocodeAddressAndCalculateRoute(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val geocoder = Geocoder(this@PatientMainActivity, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)
            addresses?.firstOrNull()?.let {
                calculateRouteToDoctor(LatLng(it.latitude, it.longitude))
            }
        }
    }

    private fun findNearbyPlaces(type: String, radius: Int) {
        currentLocation?.let {
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${it.latitude},${it.longitude}&radius=$radius&type=$type&key=YOUR_API_KEY"
            CoroutineScope(Dispatchers.IO).launch {
                val response = URL(url).readText()
                val json = JSONObject(response)
                val results = json.getJSONArray("results")
                withContext(Dispatchers.Main) {
                    mMap.clear()
                    for (i in 0 until results.length()) {
                        val place = results.getJSONObject(i)
                        val location = place.getJSONObject("geometry").getJSONObject("location")
                        val lat = location.getDouble("lat")
                        val lng = location.getDouble("lng")
                        val name = place.getString("name")
                        mMap.addMarker(MarkerOptions().position(LatLng(lat, lng)).title(name))
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                currentLocation = result.lastLocation
                currentLocation?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(latLng).title("You are here"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }
}
