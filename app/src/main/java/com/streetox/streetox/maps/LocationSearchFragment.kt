package com.streetox.streetox.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentLocationSearchBinding
import com.streetox.streetox.viewmodels.Stateviewmodels.StateFromLocation
import com.streetox.streetox.viewmodels.Stateviewmodels.StateFromlatLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class LocationSearchFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraIdleListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var binding: FragmentLocationSearchBinding
    private val searchQueryViewModel by activityViewModels<StateFromLocation>()
    private val latLngViewModel by activityViewModels<StateFromlatLong>()
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mGoogleMap: GoogleMap? = null
    private var fragmentContext: Context? = null
    private var bottomNavigationView: BottomNavigationView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLocationSearchBinding.inflate(inflater, container, false)

        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.GONE

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.location_Search_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize Fused Location Provider Client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search when user submits query
                query?.let { searchLocation(it) }
                return true
            }

            private fun searchLocation(query: String) {
                // Start a coroutine to perform geocoding asynchronously
                viewLifecycleOwner.lifecycleScope.launch {
                    val address = geocodeLocation(query)
                    address?.let { showLocationOnMap(it, query) }
                }
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle search as the query text changes (optional)
                return false
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_locationSearchFragment_to_fromFragment)
            }

        })
        on_btn_go_click()

        return binding.root
    }

    private fun on_btn_go_click() {
        binding.btnGo.setOnClickListener {
            val searchQuery = binding.searchView.query.toString()
            val markerPosition = mGoogleMap?.cameraPosition?.target

            if (markerPosition != null) {
                val latitude = markerPosition.latitude
                val longitude = markerPosition.longitude


                searchQueryViewModel.setSearchQuery(searchQuery)
                latLngViewModel.setLatitude(latitude)
                latLngViewModel.setLongitude(longitude)

                findNavController().navigate(R.id.action_locationSearchFragment_to_fromFragment)
            } else {
                // Handle the case where the marker position is not available
                // You may display a message or take other appropriate action
                Log.e("LocationSearchFragment", "Marker position is null")
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap


//        mGoogleMap?.isMyLocationEnabled = true
//
//        val locationButton = (view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(Integer.parseInt("2"))
//        val rlp =  locationButton.getLayoutParams() as RelativeLayout.LayoutParams
//        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
//        rlp.setMargins(0, 250, 10, 0)


        try {
            val success = mGoogleMap!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.streetox_dark_with_label
                )
            )
            if (!success) {
                Log.d("polymapcostmer", "Failed to load map style")
            }
        } catch (ex: Resources.NotFoundException) {
            Log.d("polymapcostmer", "Not found json string for map style")
        }


        mGoogleMap!!.getUiSettings().setCompassEnabled(false)

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If permission not granted, request it
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Get the last known location of the user
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location ->
            // If the location is not null, move the camera to the user's last known location
            location?.let {
                val latLng = LatLng(location.latitude, location.longitude)
                mGoogleMap?.apply {
                    addMarker(
                        MarkerOptions().position(latLng).title("Your Location").draggable(
                            false
                        )
                    )
                    animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                    setOnCameraMoveListener(this@LocationSearchFragment)
                    setOnCameraIdleListener(this@LocationSearchFragment)
                    setOnMapClickListener { binding.searchView.clearFocus() }
                }
            }
        }
    }

    override fun onCameraMove() {
        mGoogleMap?.clear()
        binding.imgLocationPinUp?.visibility = View.VISIBLE
    }

    override fun onCameraIdle() {
        binding.imgLocationPinUp?.visibility = View.GONE
        val markerOptions = MarkerOptions().position(mGoogleMap?.cameraPosition!!.target)


        val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.search_pin)
        markerOptions.icon(null)
        val marker = mGoogleMap?.addMarker(markerOptions)
        val markerLatLng = marker?.position
        val latitude = markerLatLng?.latitude
        val longitude = markerLatLng?.longitude

        val address = getLocationName(latitude ?: 0.0, longitude ?: 0.0)
        binding.searchView.setQuery(address, false)
    }

    private suspend fun geocodeLocation(query: String): Address? {
        return withContext(Dispatchers.IO) {
            val geoCoder = Geocoder(requireContext())
            try {
                val addressList = geoCoder.getFromLocationName(query, 1)
                if (addressList!!.isNotEmpty()) {
                    return@withContext addressList!![0]
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return@withContext null
        }
    }

    private fun showLocationOnMap(address: Address, query: String) {
        val latLng = LatLng(address.latitude, address.longitude)
        mGoogleMap?.clear()
        mGoogleMap?.addMarker(MarkerOptions().position(latLng).title(query))
        mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }


    private fun getLocationName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(fragmentContext!!)
        try {
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty()!!) {
                val address = addresses?.get(0)
                val buildingName = address?.featureName ?: ""
                val subBuildingName = address?.subThoroughfare ?: ""
                val thoroughfare = address?.thoroughfare ?: ""
                val subLocality = address?.subLocality ?: ""
                val locality = address?.locality ?: ""
                val adminArea = address?.adminArea ?: ""
                val countryName = address?.countryName ?: ""
                val postalCode = address?.postalCode ?: ""

                val fullAddress = buildString {
                    if (buildingName.isNotBlank()) append("$buildingName, ")
                    if (subBuildingName.isNotBlank()) append("$subBuildingName, ")
                    if (thoroughfare.isNotBlank()) append("$thoroughfare, ")
                    if (subLocality.isNotBlank()) append("$subLocality, ")
                    if (locality.isNotBlank()) append("$locality, ")
                    if (adminArea.isNotBlank()) append("$adminArea")
                    if (countryName.isNotBlank()) append("$countryName")
                    if (postalCode.isNotBlank()) append("$postalCode")
                }

                Log.d("nameaddress", fullAddress)
                return fullAddress
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}
