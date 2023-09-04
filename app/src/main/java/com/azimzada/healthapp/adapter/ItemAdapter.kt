package com.azimzada.healthapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azimzada.healthapp.R
import com.azimzada.healthapp.room.model.FitData

class ItemAdapter(private val itemList: List<FitData>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val currentItem = itemList[position]

        holder.bind(currentItem)

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stepCountTextView: TextView = itemView.findViewById(R.id.step)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val colori: TextView = itemView.findViewById(R.id.coloriItem)
        private val activity: TextView = itemView.findViewById(R.id.activity)

        fun bind(item: FitData) {
            if (item.dateTime.length < 20) {
                stepCountTextView.visibility = View.VISIBLE
                imageView.visibility = View.VISIBLE
                stepCountTextView.text = item.stepCount.toString()
                // Calculate calories burned based on step count (simplified calculation)
                val caloriesBurned =
                    item.stepCount * 0.05 // Adjust the factor based on your calculation
                colori.text = caloriesBurned.toInt().toString()
                activity.text = item.activityDuration
            } else {
                stepCountTextView.visibility = View.GONE
                imageView.visibility = View.GONE
            }
        }
    }
}



