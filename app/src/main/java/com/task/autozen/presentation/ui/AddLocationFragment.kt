package com.task.autozen.presentation.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.task.autozen.R
import com.task.autozen.data.local.AppDatabase
import com.task.autozen.data.local.savedlocations.SavedLocationEntity
import com.task.autozen.databinding.FragmentAddLocationBinding
import com.task.autozen.domain.interfaces.ModeRepository
import com.task.autozen.domain.repository.ModeRepositoryImpl
import com.task.autozen.presentation.viewmodels.ModeViewModel
import com.task.autozen.presentation.viewmodels.ModeViewModelFactory
import com.task.autozen.services.ModeSwitchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddLocationFragment : Fragment() {

    private var _binding: FragmentAddLocationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ModeViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedMode: Int = 0 // Silent Mode by Default
    private var radius: Float = 100f
    private var locationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // from Home Fragment when edit clicked
        locationId = arguments?.getInt("locationId", -1) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository: ModeRepository = getRepository()
        viewModel =
            ViewModelProvider(this, ModeViewModelFactory(repository))[ModeViewModel::class.java]
        //auto-fill existing data in case of edit
        fillDetailsIfPresent()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // default selection Silent
        binding.toggleMode.check(R.id.btnSilent)

        setupListeners()

        //get details from SelectLocationFragment
        parentFragmentManager.setFragmentResultListener(
            "location_selected",
            viewLifecycleOwner
        ) { _, bundle ->
            val latitude = bundle.getDouble("latitude")
            val longitude = bundle.getDouble("longitude")
            binding.etLatitude.setText(latitude.toString())
            binding.etLongitude.setText(longitude.toString())
        }
    }

    private fun fillDetailsIfPresent() {
        if (locationId != -1) {
            viewModel.fetchLocationById(locationId)
        }
        viewModel.selectedLocation.observe(viewLifecycleOwner) { location ->
            location?.let {
                binding.etLocationName.setText(it.name)
                binding.etLatitude.setText(it.latitude.toString())
                binding.etLongitude.setText(it.longitude.toString())
                binding.sliderRadius.value = it.radius
                binding.tvRadiusValue.text = "Radius: ${it.radius.toInt()}"

                binding.toggleMode.check(
                    when (it.mode) {
                        0 -> R.id.btnSilent
                        1 -> R.id.btnVibrate
                        else -> R.id.btnSilent
                    }
                )
            }
        }
    }

    private fun setupListeners() {
        binding.sliderRadius.addOnChangeListener { _, value, _ ->
            radius = value
            binding.tvRadiusValue.text = "Radius: ${value.toInt()}m"
        }
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                selectedMode = when (checkedId) {
                    R.id.btnSilent -> 0
                    R.id.btnVibrate -> 1
                    else -> 0
                }
            }
        }
        binding.btnSaveLocation.setOnClickListener {
            saveLocation()
        }
        binding.btnCurrentLocation.setOnClickListener {
            getCurrentLocation()
        }
        binding.btnPickLocation.setOnClickListener {
            findNavController().navigate(R.id.action_addLocationFragment_to_selectLocationFragment)
        }
    }

    private fun saveLocation() {
        val name = binding.etLocationName.text.toString()
        val lat = binding.etLatitude.text.toString()
        val lon = binding.etLongitude.text.toString()

        if (name.isEmpty() || lat.isEmpty() || lon.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter all details", Toast.LENGTH_SHORT).show()
            return
        }

        val savedLocation = SavedLocationEntity(
            name = name,
            latitude = lat.toDouble(),
            longitude = lon.toDouble(),
            radius = radius,
            mode = selectedMode
        )

        viewModel.saveLocation(savedLocation)
        // for immediate update of Ringer Mode
        val intent = Intent(requireContext(), ModeSwitchService::class.java).apply {
            action = "UPDATE_MODE"
        }
        requireContext().startService(intent)
        Toast.makeText(requireContext(), "Location Saved!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                binding.etLatitude.setText(location.latitude.toString())
                binding.etLongitude.setText(location.longitude.toString())
            } else {
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT)
                    .show()
            }
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }
}