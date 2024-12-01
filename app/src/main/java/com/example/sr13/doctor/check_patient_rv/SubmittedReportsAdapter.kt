package com.example.sr13.doctor.check_patient_rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sr13.R

/**
 * Adapter class for displaying a list of submitted reports in a RecyclerView.
 *
 * @property mList The list of submitted reports to display.
 * @property onItemClick A lambda function to handle click events for each report item.
 */
class SubmittedReportsAdapter(
    private var mList: List<SubmittedReportsViewModel>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SubmittedReportsAdapter.ViewHolder>() {

    /**
     * Called when RecyclerView needs a new ViewHolder to represent an item.
     *
     * @param parent The parent ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.submitted_reports_cardview, parent, false)
        return ViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder The ViewHolder to be updated with new data.
     * @param position The position of the item in the data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val submittedReport = mList[position]
        holder.imageView.setImageResource(submittedReport.image)
        holder.patientFullName.text = submittedReport.patientId
        holder.reportDate.text = submittedReport.reportDate

        // Set the click listener for the item to handle user interactions.
        holder.itemView.setOnClickListener {
            onItemClick(submittedReport.reportId)
        }
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return The size of the data set.
     */
    override fun getItemCount(): Int {
        return mList.size
    }

    /**
     * Updates the list of submitted reports and notifies the adapter to refresh the view.
     *
     * @param newList The new list of submitted reports.
     */
    fun updateList(newList: List<SubmittedReportsViewModel>) {
        mList = newList
        notifyDataSetChanged() // Notify RecyclerView to refresh its data.
    }

    /**
     * ViewHolder class for holding the views associated with each report item.
     *
     * @constructor Creates a ViewHolder instance.
     * @param ItemView The item view layout representing a single report.
     */
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview) // Image representing the report.
        val patientFullName: TextView = itemView.findViewById(R.id.submittedReportPatientName) // Patient's full name.
        val reportDate: TextView = itemView.findViewById(R.id.submittedReportDate) // Date of the report.
    }
}