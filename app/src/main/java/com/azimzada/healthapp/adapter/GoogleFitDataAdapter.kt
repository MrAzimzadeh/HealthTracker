package com.azimzada.healthapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azimzada.healthapp.R
import com.azimzada.healthapp.dtos.GoogleFitDataDTO

class GoogleFitDataAdapter(private val googleFitDataList: List<GoogleFitDataDTO>) :
    RecyclerView.Adapter<GoogleFitDataAdapter.GoogleFitDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoogleFitDataViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return GoogleFitDataViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GoogleFitDataViewHolder, position: Int) {
        val currentItem = googleFitDataList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return googleFitDataList.size
    }

    inner class GoogleFitDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val dateTextView: TextView = itemView.findViewById(R.id.date)
        private val stepCountTextView: TextView = itemView.findViewById(R.id.step)
//        private val activityTextView: TextView = itemView.findViewById(R.id.activity)
//        private val caloriesTextView: TextView = itemView.findViewById(R.id.calories)

        fun bind(item: GoogleFitDataDTO) {
//            dateTextView.text = item.date
            stepCountTextView.text = "Steps: ${item.steps}"
//            activityTextView.text = "Activity: ${item.activity}"
//            caloriesTextView.text = "Calories: ${item.calories}"

            // Burada dilediğiniz gibi görünümü güncelleyebilirsiniz
        }
    }
}