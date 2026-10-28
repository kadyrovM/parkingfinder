package com.example.parkingfinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ParkingAdapter(
    private val items: List<ParkingPlace>,
    private val onClick: (ParkingPlace) -> Unit
) : RecyclerView.Adapter<ParkingAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = "🅿 ${item.name}"
        holder.tvAddress.text = item.address

        // Расстояние: метры или км
        holder.tvDistance.text = if (item.distanceMeters < 1000)
            "📍 ${item.distanceMeters.toInt()} м"
        else
            "📍 ${"%.1f".format(item.distanceMeters / 1000)} км"

        holder.tvRating.text = if (item.rating > 0) "⭐ ${item.rating}" else ""
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}