package com.streetox.streetox.maps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentLocationSearchBinding
import java.io.IOException

class LocationSearchFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var binding: FragmentLocationSearchBinding
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
        val mapFragment = childFragmentManager.findFragmentById(R.id.location_Search_map) as SupportMapFragment
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
                val geoCoder = Geocoder(requireContext())
                try {
                    val addressList = geoCoder.getFromLocationName(query, 1)
                    if (addressList != null) {
                        if (addressList.isNotEmpty()) {
                            val address = addressList?.get(0)
                            val latLng = LatLng(address?.latitude!!, address?.longitude!!)
                            mGoogleMap?.clear() // Clear previous markers
                            mGoogleMap?.addMarker(MarkerOptions().position(latLng).title(query))
                            mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle search as the query text changes (optional)
                return false
            }
        })



        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

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
                    addMarker(MarkerOptions().position(latLng).title("Your Location").draggable(false))
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


        val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.user_curr_location_marker)
        markerOptions.icon(customMarkerIcon)
        val marker = mGoogleMap?.addMarker(markerOptions)
        val markerLatLng = marker?.position
        val latitude = markerLatLng?.latitude
        val longitude = markerLatLng?.longitude

        val address = getLocationName(latitude ?: 0.0, longitude ?: 0.0)
        binding.searchView.setQuery(address, false)
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
