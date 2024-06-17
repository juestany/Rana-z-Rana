package com.example.sr13.doctor.check_patient_rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sr13.R

class SubmittedReportsAdapter (
    private val mList: List<SubmittedReportsViewModel>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SubmittedReportsAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.submitted_reports_cardview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val SubmittedReportsViewModel = mList[position]
        holder.imageView.setImageResource(SubmittedReportsViewModel.image)
        holder.patientFullName.text = SubmittedReportsViewModel.patientId
        holder.reportDate.text = SubmittedReportsViewModel.reportDate

        // Set the click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(SubmittedReportsViewModel.reportId)
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val patientFullName: TextView = itemView.findViewById(R.id.submittedReportPatientName)
        val reportDate: TextView = itemView.findViewById(R.id.submittedReportDate)
    }
}
