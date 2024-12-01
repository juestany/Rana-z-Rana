package com.example.sr13.doctor

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sr13.R
import com.example.sr13.firestore.Message
import com.example.sr13.messages_rv.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for real-time chat between users.
 * Handles sending and receiving messages in a specific chat room.
 */
class ChatActivity : AppCompatActivity() {

    // Firebase Firestore instance for database operations
    private lateinit var firestore: FirebaseFirestore

    // ID of the chat room to load and send messages
    private lateinit var chatRoomId: String

    // UI elements
    private lateinit var messagesRecyclerView: RecyclerView // RecyclerView for displaying messages
    private lateinit var messageAdapter: MessageAdapter // Adapter for managing messages in the RecyclerView
    private lateinit var sendMessageButton: Button // Button to send a new message
    private lateinit var messageInput: EditText // EditText for inputting new messages
    private lateinit var participantNameTextView: TextView // TextView for displaying the participant's name

    // List to hold chat messages
    private val messagesList = mutableListOf<Message>()

    /**
     * Called when the activity is first created.
     * Initializes Firebase, sets up the UI, and loads messages.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Retrieve chat room ID and participant name from intent extras
        chatRoomId = intent.getStringExtra("CHAT_ROOM_ID") ?: "" // Default to an empty string if not provided
        val participantName = intent.getStringExtra("PARTICIPANT_NAME") ?: "Unknown"

        firestore = FirebaseFirestore.getInstance() // Initialize Firestore

        // Initialize UI components
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        messageInput = findViewById(R.id.messageInput)
        participantNameTextView = findViewById(R.id.participantNameTextView)

        // Set the participant's name in the UI
        participantNameTextView.text = participantName

        // Set up RecyclerView with an adapter
        messageAdapter = MessageAdapter(messagesList)
        messagesRecyclerView.adapter = messageAdapter
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load existing messages from Firestore
        loadMessages()

        // Set up click listener for sending messages
        sendMessageButton.setOnClickListener {
            sendMessage()
        }
    }

    /**
     * Loads chat messages from Firestore and listens for real-time updates.
     * Updates the RecyclerView with the latest messages.
     */
    private fun loadMessages() {
        firestore.collection("chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp") // Ensure messages are displayed in chronological order
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("ChatActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                messagesList.clear() // Clear the list before adding updated messages
                for (doc in snapshots!!) {
                    val message = doc.toObject(Message::class.java) // Convert Firestore document to Message object
                    messagesList.add(message)
                }

                messageAdapter.notifyDataSetChanged() // Notify adapter to refresh RecyclerView
                messagesRecyclerView.scrollToPosition(messagesList.size - 1) // Scroll to the last message
            }
    }

    /**
     * Sends a new message to the Firestore chat room.
     * Clears the input field after successfully sending the message.
     */
    private fun sendMessage() {
        val messageText = messageInput.text.toString() // Get message from input field
        val senderId = FirebaseAuth.getInstance().currentUser?.uid // Get the sender's user ID

        if (messageText.isNotEmpty() && senderId != null) { // Ensure message is not empty and user is authenticated
            // Create a map for the message data
            val messageData = hashMapOf(
                "senderId" to senderId, // Sender's ID
                "message" to messageText, // The message text
                "timestamp" to FieldValue.serverTimestamp() // Server-generated timestamp
            )

            // Add the message to Firestore
            firestore.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener {
                    // Clear the input field after sending the message
                    messageInput.text.clear()
                }
                .addOnFailureListener { e ->
                    Log.e("ChatActivity", "Error sending message: ${e.message}") // Log error
                    Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Notify user to enter a valid message
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }
}