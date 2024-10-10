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

class MessageAdapter(private val messageList: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    // View types for sent and received messages
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

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

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    // ViewHolder class that handles both sent and received messages
    class MessageViewHolder(itemView: View, private val viewType: Int) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.messageTimestamp)
        private var senderProfileImage: ImageView? = null

        // Only find profile image in received messages
        init {
            if (viewType == 2) {
                senderProfileImage = itemView.findViewById(R.id.senderProfileImage)
            }
        }

        fun bind(message: Message) {
            messageTextView.text = message.message

            // Format timestamp if it's not null
            message.timestamp?.let { timestamp ->
                val currentCalendar = Calendar.getInstance()
                val messageCalendar = Calendar.getInstance().apply { time = timestamp.toDate() }

                // Calculate the difference in days
                val daysDifference = currentCalendar.get(Calendar.DAY_OF_YEAR) - messageCalendar.get(Calendar.DAY_OF_YEAR)
                val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate())

                messageTimestamp.text = when {
                    daysDifference == 0 -> formattedTime // Today
                    daysDifference == 1 -> "wczoraj $formattedTime" // Yesterday in Polish
                    daysDifference in 2..6 -> {
                        // Day of the week in Polish
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
                        "$dayOfWeekShort $formattedTime" // Day of the week + time
                    }
                    else -> {
                        // For more than 6 days, format as "dd.MM.yyyy – HH:mm"
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        "${
                            dateFormat.format(timestamp.toDate())
                        } – $formattedTime" // Full date + time
                    }
                }
            } ?: run {
                messageTimestamp.text = "" // Handle null case
            }

            // If the message is received, load the sender's profile picture
            if (viewType == 2 && senderProfileImage != null) {
                loadSenderProfileImage(message.senderId)
            }
        }

        // Function to load profile image from Firestore
        private fun loadSenderProfileImage(senderId: String) {
            val firestore = FirebaseFirestore.getInstance()
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

            // If not found in patient collection, search in doctor collection
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