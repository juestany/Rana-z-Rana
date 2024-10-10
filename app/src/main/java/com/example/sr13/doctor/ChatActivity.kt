package com.example.sr13.doctor

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
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
    private lateinit var messageAdapter: MessageAdapter // Assuming you have a MessageAdapter class
    private lateinit var sendMessageButton: Button
    private lateinit var messageInput: EditText

    private val messagesList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatRoomId = intent.getStringExtra("CHAT_ROOM_ID") ?: ""
        firestore = FirebaseFirestore.getInstance()

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        sendMessageButton = findViewById(R.id.sendMessageButton)
        messageInput = findViewById(R.id.messageInput)

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
        if (messageText.isNotEmpty()) {
            val messageData = hashMapOf(
                "senderId" to FirebaseAuth.getInstance().currentUser?.uid,
                "message" to messageText,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener {
                    // Clear the input field after sending
                    messageInput.text.clear()
                }
                .addOnFailureListener { e ->
                    // Handle any errors
                }
        }
    }
}