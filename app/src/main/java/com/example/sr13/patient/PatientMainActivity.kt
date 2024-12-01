package com.example.sr13.patient

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale

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

    /**
     * Initializes the activity, sets up Firebase, UI components, and map fragment.
     * Also requests location updates and registers the device for notifications.
     */
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
        Places.initialize(applicationContext, "AIzaSyCcRzRGfnozt57TuI03DIe_PyfqSfwjwrg")

        // Load patient data and set up listeners
        getPatientData()
        setupListeners()

        // Setup map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        requestLocationUpdates()
        registerDeviceToken()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Messages"
            val descriptionText = "Notifications for new messages"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("messages_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }


    /**
     * Sets up listeners for various buttons and user actions on the main screen.
     * Includes functionality for submitting a report, logging out, and interacting with the map.
     */
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

    /**
     * Fetches and displays the patient's data from Firestore.
     * Updates the UI with the patient's name, profile picture, and doctor's information.
     */
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

    /**
     * Checks if a chat room exists between the patient and their doctor.
     * Creates a new chat room if one does not exist.
     */
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

    /**
     * Fetches the doctor's name from Firestore based on the doctor ID.
     * Calls the provided callback function with the doctor's full name.
     */
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

    /**
     * Creates a new chat room in Firestore with the given doctor and patient IDs.
     * Opens the chat screen after successfully creating the chat room.
     */
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

    /**
     * Opens the chat activity for the specified chat room ID and participant name.
     */
    private fun openChat(chatRoomId: String, participantName: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("CHAT_ROOM_ID", chatRoomId)
            putExtra("PARTICIPANT_NAME", participantName)
        }
        startActivity(intent)
    }

    /**
     * Fetches the doctor's location from Firestore using their ID.
     * Geocodes the doctor's address and stores the resulting LatLng for routing.
     */
    private fun getDoctorLocation(doctorId: String) {
        firestore.collection("doctor").document(doctorId).get().addOnSuccessListener { document ->
            document?.getString("adress")?.let { address ->
                geocodeDoctorAddress(address)
            }
        }
    }

    /**
     * Geocodes the given address into a LatLng object using a Geocoder.
     * Stores the resulting coordinates in the doctorLocation variable.
     */
    private fun geocodeDoctorAddress(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val geocoder = Geocoder(this@PatientMainActivity, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)
            addresses?.firstOrNull()?.let {
                doctorLocation = LatLng(it.latitude, it.longitude)
            }
        }
    }


    /**
     * Calculates a route from the patient's current location to the doctor's location.
     * Opens Google Maps with directions to the doctor's location.
     */
    private fun calculateRouteToDoctor(currentLatLng: LatLng) {
        doctorLocation?.let {
            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${currentLatLng.latitude},${currentLatLng.longitude}&destination=${it.latitude},${it.longitude}&travelmode=driving")
            val intent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }
    }

    /**
     * Geocodes the patient's address and calculates a route to the doctor's location.
     * Uses a Geocoder to convert the address into coordinates.
     */
    private fun geocodeAddressAndCalculateRoute(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val geocoder = Geocoder(this@PatientMainActivity, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)
            addresses?.firstOrNull()?.let {
                calculateRouteToDoctor(LatLng(it.latitude, it.longitude))
            }
        }
    }

    /**
     * Finds nearby places of a specified type (e.g., pharmacy, hospital) within a given radius.
     * Marks the found places on the Google Map using their coordinates.
     */
    private fun findNearbyPlaces(type: String, radius: Int) {
        currentLocation?.let {
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${it.latitude},${it.longitude}&radius=$radius&type=$type&key=AIzaSyCcRzRGfnozt57TuI03DIe_PyfqSfwjwrg"
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

    /**
     * Called when the Google Map is ready to be used.
     * Enables zoom controls on the map.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
    }


    /**
     * Requests regular location updates from the device's location services.
     * Updates the map with the patient's current location.
     */
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

    /**
     * Registers the device with Firebase for push notifications.
     * Retrieves the device's FCM token and saves it to Firestore.
     */
    private fun registerDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "Token wygenerowany: $token") // Log wygenerowanego tokena
                saveTokenToFirestore(token)
            } else {
                Log.e("FCM", "Błąd podczas generowania tokena", task.exception)
            }
        }
    }


    /**
     * Saves the provided Firebase Cloud Messaging (FCM) token to Firestore.
     * Associates the token with the current user's ID.
     */
    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val userDoc = FirebaseFirestore.getInstance().collection("users").document(it)
            userDoc.update("deviceToken", token).addOnSuccessListener {
                Log.d("FCM", "Token saved successfully")
            }.addOnFailureListener { e ->
                Log.e("FCM", "Failed to save token", e)
            }
        }
    }
}
