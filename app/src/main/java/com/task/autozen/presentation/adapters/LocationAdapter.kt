package com.task.autozen.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.task.autozen.data.local.savedlocations.SavedLocationEntity
import com.task.autozen.databinding.ItemLocationBinding
import com.task.autozen.utils.PhoneMode

class LocationAdapter(
    private var locations: List<SavedLocationEntity>,
    private val onDeleteClick: (SavedLocationEntity) -> Unit,
    private val onEditClick: (SavedLocationEntity) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    inner class LocationViewHolder(private val binding: ItemLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(location: SavedLocationEntity) {
            binding.locationName.text = location.name
            binding.locationDetails.text = PhoneMode.fromInt(location.mode).name

            binding.deleteButton.setOnClickListener {
                onDeleteClick(location)
            }
            binding.editButton.setOnClickListener {
                onEditClick(location)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(locations[position])
    }

    override fun getItemCount(): Int = locations.size

    fun updateList(newLocations: List<SavedLocationEntity>) {
        locations = newLocations
        notifyDataSetChanged()
    }
}