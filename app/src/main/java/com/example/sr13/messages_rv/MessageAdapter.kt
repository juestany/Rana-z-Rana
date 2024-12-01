package com.example.sr13.messages_rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sr13.R
import com.example.sr13.firestore.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Adapter for displaying chat messages in a RecyclerView.
 * Handles both sent and received messages with different layouts.
 *
 * @param messageList List of messages to display.
 */
class MessageAdapter(private val messageList: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    // Current user's ID for distinguishing sent and received messages
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // View types for sent and received messages
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    /**
     * Inflates the appropriate layout based on the message type (sent or received).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = if (viewType == VIEW_TYPE_SENT) {
            // Inflate sent message layout
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
        } else {
            // Inflate received message layout
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
        }
        return MessageViewHolder(view, viewType)
    }

    /**
     * Binds the message data to the ViewHolder.
     */
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.bind(message)
    }

    /**
     * Returns the total number of messages in the list.
     */
    override fun getItemCount(): Int = messageList.size

    /**
     * Determines the view type for a message based on the sender.
     */
    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    /**
     * ViewHolder class to manage the views for each message.
     *
     * @param itemView The layout for the message (sent or received).
     * @param viewType The type of message (sent or received).
     */
    class MessageViewHolder(itemView: View, private val viewType: Int) : RecyclerView.ViewHolder(itemView) {

        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.messageTimestamp)
        private var senderProfileImage: ImageView? = null

        // Initialize the profile image view for received messages
        init {
            if (viewType == 2) { // VIEW_TYPE_RECEIVED
                senderProfileImage = itemView.findViewById(R.id.senderProfileImage)
            }
        }

        /**
         * Binds the message data to the views.
         */
        fun bind(message: Message) {
            // Set message text
            messageTextView.text = message.message

            // Format and display the timestamp
            message.timestamp?.let { timestamp ->
                val currentCalendar = Calendar.getInstance()
                val messageCalendar = Calendar.getInstance().apply { time = timestamp.toDate() }

                // Calculate days difference to determine the format
                val daysDifference = currentCalendar.get(Calendar.DAY_OF_YEAR) - messageCalendar.get(Calendar.DAY_OF_YEAR)
                val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())

                messageTimestamp.text = when {
                    daysDifference == 0 -> formattedTime // Today
                    daysDifference == 1 -> "wczoraj $formattedTime" // Yesterday
                    daysDifference in 2..6 -> {
                        // Show day of the week for the past week
                        val dayOfWeekShort = when (messageCalendar.get(Calendar.DAY_OF_WEEK)) {
                            Calendar.MONDAY -> "pon."
                            Calendar.TUESDAY -> "wt."
                            Calendar.WEDNESDAY -> "śr."
                            Calendar.THURSDAY -> "czw."
                            Calendar.FRIDAY -> "pt."
                            Calendar.SATURDAY -> "sob."
                            Calendar.SUNDAY -> "niedz."
                            else -> ""
                        }
                        "$dayOfWeekShort $formattedTime"
                    }
                    else -> {
                        // Full date for older messages
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        "${dateFormat.format(timestamp.toDate())} – $formattedTime"
                    }
                }
            } ?: run {
                messageTimestamp.text = "" // Handle null timestamps
            }

            // Load sender's profile image for received messages
            if (viewType == 2 && senderProfileImage != null) {
                loadSenderProfileImage(message.senderId)
            }
        }

        /**
         * Loads the sender's profile image from Firestore and displays it.
         *
         * @param senderId The ID of the sender.
         */
        private fun loadSenderProfileImage(senderId: String) {
            val firestore = FirebaseFirestore.getInstance()

            // First, check in the patient collection
            firestore.collection("patient").document(senderId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val imageUrl = documentSnapshot.getString("imageId")
                    if (imageUrl != null) {
                        Glide.with(itemView.context)
                            .load(imageUrl)
                            .into(senderProfileImage!!)
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }

            // If not found, check in the doctor collection
            firestore.collection("doctor").document(senderId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val imageUrl = documentSnapshot.getString("imageId")
                    if (imageUrl != null) {
                        Glide.with(itemView.context)
                            .load(imageUrl)
                            .into(senderProfileImage!!)
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }
        }
    }
}