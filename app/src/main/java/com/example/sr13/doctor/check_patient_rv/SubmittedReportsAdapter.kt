package com.example.sr13.doctor.check_patient_rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sr13.R

class SubmittedReportsAdapter (
    private var mList: List<SubmittedReportsViewModel>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SubmittedReportsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.submitted_reports_cardview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val submittedReport = mList[position]
        holder.imageView.setImageResource(submittedReport.image)
        holder.patientFullName.text = submittedReport.patientId
        holder.reportDate.text = submittedReport.reportDate

        // Set the click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(submittedReport.reportId)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    // Method to update the list when new data comes in
    fun updateList(newList: List<SubmittedReportsViewModel>) {
        mList = newList
        notifyDataSetChanged()  // Notify the adapter to refresh the view
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val patientFullName: TextView = itemView.findViewById(R.id.submittedReportPatientName)
        val reportDate: TextView = itemView.findViewById(R.id.submittedReportDate)
    }
}
