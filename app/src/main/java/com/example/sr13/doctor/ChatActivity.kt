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

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRoomId: String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var sendMessageButton: Button
    private lateinit var messageInput: EditText
    private lateinit var participantNameTextView: TextView // Reference to the participant name TextView

    private val messagesList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatRoomId = intent.getStringExtra("CHAT_ROOM_ID") ?: ""
        firestore = FirebaseFirestore.getInstance()

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        messageInput = findViewById(R.id.messageInput)
        participantNameTextView = findViewById(R.id.participantNameTextView)

        // Set the participant's name
        val participantName = intent.getStringExtra("PARTICIPANT_NAME") ?: "Unknown"
        participantNameTextView.text = participantName

        messageAdapter = MessageAdapter(messagesList)
        messagesRecyclerView.adapter = messageAdapter
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Load chat messages
        loadMessages()

        sendMessageButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun loadMessages() {
        // Query Firestore for chat messages in the specified chatRoomId
        firestore.collection("chats")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Chat", "Listen failed.", e)
                    return@addSnapshotListener
                }

                messagesList.clear()
                for (doc in snapshots!!) {
                    val message = doc.toObject(Message::class.java)
                    messagesList.add(message)
                }

                messageAdapter.notifyDataSetChanged()
                messagesRecyclerView.scrollToPosition(messagesList.size - 1) // Scroll to the last message
            }
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString()
        val senderId = FirebaseAuth.getInstance().currentUser?.uid

        if (messageText.isNotEmpty() && senderId != null) { // Check if message is not empty and senderId is not null
            // Create a message data map
            val messageData = hashMapOf(
                "senderId" to senderId,
                "message" to messageText,
                "timestamp" to FieldValue.serverTimestamp() // Use Firestore's server timestamp
            )

            // Add the message to Firestore
            firestore.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener {
                    // Clear the input field after sending
                    messageInput.text.clear()
                }
                .addOnFailureListener { e ->
                    // Handle any errors, e.g. log or show a Toast
                    Log.e("ChatActivity", "Error sending message: ${e.message}")
                    Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Optionally, you can show a message to the user if the input is empty or user is not authenticated
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }
}