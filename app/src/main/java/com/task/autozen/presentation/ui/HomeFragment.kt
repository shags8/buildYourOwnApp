package com.task.autozen.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.task.autozen.R
import com.task.autozen.data.local.AppDatabase
import com.task.autozen.databinding.FragmentHomeBinding
import com.task.autozen.domain.interfaces.ModeRepository
import com.task.autozen.domain.repository.ModeRepositoryImpl
import com.task.autozen.presentation.adapters.LocationAdapter
import com.task.autozen.presentation.viewmodels.ModeViewModel
import com.task.autozen.presentation.viewmodels.ModeViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ModeViewModel by viewModels {
        ModeViewModelFactory(getRepository())
    }
    private lateinit var adapter: LocationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        binding.addLocationButton.setOnClickListener {
            findNavController().navigate(R.id.addLocationFragment)
        }
        binding.fabAddLocation.setOnClickListener {
            findNavController().navigate(R.id.addLocationFragment)
        }

    }


    private fun setupRecyclerView() {
        binding.locationsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LocationAdapter(
            locations = emptyList(),
            onDeleteClick = { location -> viewModel.deleteLocation(location.id) },
            onEditClick = { location ->
                val bundle = Bundle().apply {
                    putInt("locationId", location.id)
                }
                findNavController().navigate(R.id.addLocationFragment, bundle)
            }
        )
        binding.locationsRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.savedLocations.observe(viewLifecycleOwner, Observer { locations ->
            if (locations.isEmpty()) {
                updateUI(true)
            } else {
                updateUI(false)
            }
            adapter.updateList(locations)
        })

        viewModel.loadSavedLocations()
    }

    private fun updateUI(isEmpty: Boolean) {
        binding.apply {
            locationsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            fabAddLocation.visibility = if (isEmpty) View.GONE else View.VISIBLE
            addLocationButton.visibility = if (isEmpty) View.VISIBLE else View.GONE
            addLocationTV.visibility = if (isEmpty) View.VISIBLE else View.GONE
            noSavedLocationTV.visibility = if (isEmpty) View.VISIBLE else View.GONE
            displayImage.visibility = if (isEmpty) View.VISIBLE else View.GONE
            tvSavedLocations.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun getRepository(): ModeRepository {
        val database = AppDatabase.getInstance(requireContext())
        return ModeRepositoryImpl(database.savedLocationDao())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}