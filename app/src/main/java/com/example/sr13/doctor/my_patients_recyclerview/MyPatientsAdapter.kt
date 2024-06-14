package com.example.sr13.doctor.my_patients_recyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sr13.R

class MyPatientsAdapter(
    private val mList: List<MyPatientsViewModel>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<MyPatientsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_patients_cardview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val MyPatientsViewModel = mList[position]
        holder.imageView.setImageResource(MyPatientsViewModel.image)
        holder.textView.text = MyPatientsViewModel.text

        // Set the click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(MyPatientsViewModel.text)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}
