package com.streetox.streetox.fragments.user

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Rect
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryDataEventListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.streetox.streetox.Listeners.IOnLoadLocationListener

import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.Utils.calculateDistance
import com.streetox.streetox.adapters.SearchNotificationAdapter
import com.streetox.streetox.databinding.FragmentSearchBinding
import com.streetox.streetox.models.LocationEvent


import com.streetox.streetox.models.MyLatLng
import com.streetox.streetox.models.notification_content
import com.streetox.streetox.service.LocationService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException


class SearchFragment : Fragment(), OnMapReadyCallback, IOnLoadLocationListener,
    GeoQueryDataEventListener {

    private var mMap: GoogleMap? = null
    private lateinit var binding: FragmentSearchBinding
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var currentMarker: Marker? = null
    private lateinit var myLocationRef: DatabaseReference
    private var lastLocation: Location? = null
    private lateinit var geoFire: GeoFire
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var notificationRecyclerview: RecyclerView
    private lateinit var notificationArrayList: ArrayList<notification_content>
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var initialPeekHeight = 0
    private var fragmentContext: Context? = null
    private var bottomNavigationView: BottomNavigationView? = null

    private var searchMarker: Marker? = null
// private lateinit var autocompleteFragment: AutocompleteSupportFragment

    private lateinit var markerdatabaseReference: DatabaseReference
//for inarea marker

    // SharedPreferences keys
    private val PREFS_NAME = "LocationPrefs"
    private val KEY_LATITUDE = "latitude"
    private val KEY_LONGITUDE = "longitude"

    // SharedPreferences instance
    private lateinit var sharedPreferences: SharedPreferences


    private var service: Intent? = null

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null


    private val backgroundLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {

            }
        }


    private val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            backgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                    }
                }

                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                    // Permission granted for fine location
                }
            }
        }


    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        bottomNavigationView = activity?.findViewById(R.id.bottom_nav_view)
        bottomNavigationView?.visibility = View.VISIBLE
// FOR SCREEN NOT MOVE UP WITH KEYBOARD
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)


        binding = FragmentSearchBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("notifications")

        service = Intent(requireContext(), LocationService::class.java)


// Initialize SharedPreferences
        sharedPreferences =
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

//RECYCLER VIEW AND LIST INITIALIZE

        notificationRecyclerview = binding.searchRecyclerview
        notificationRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        notificationRecyclerview.setHasFixedSize(true)
        notificationArrayList = arrayListOf<notification_content>()

// DIVIDER FOR RECYCLER VIEW

        val dividerItemDecoration =
            object : DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL) {
                override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                    val dividerLeft = parent.paddingLeft
                    val dividerRight = parent.width - parent.paddingRight

                    val childCount = parent.childCount
                    for (i in 0 until childCount - 1) { // Iterate over all items except the last one
                        val child = parent.getChildAt(i)
                        val params = child.layoutParams as RecyclerView.LayoutParams

                        val dividerTop = child.bottom + params.bottomMargin
                        val dividerBottom = dividerTop + (drawable?.intrinsicHeight ?: 0)

                        drawable?.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                        drawable?.draw(c)
                    }
                }
            }

        ResourcesCompat.getDrawable(resources, R.drawable.divider, null)?.let { drawable ->
            dividerItemDecoration.setDrawable(drawable)
        }

        binding.searchRecyclerview.addItemDecoration(dividerItemDecoration)


        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }

        })


// SET THE ON QUERY CHANGE TEXT LISTENER ON SEARCH VIEW

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                clearNotificationList()
// This method is called when the user submits the query
// Call the searchLocation() function here
                searchlocation()
// Return true to indicate that the query has been handled
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {

// Clear the notification list when the search query is empty
                    clearNotificationList()
                } else {
// Otherwise, do nothing or perform any other actions if needed
                }
// Return true to indicate that the query has been handled
                return true
            }
        })


//uses places api rest we are jot using it right now
// Places.initialize(requireContext(),getString(R.string.google_map_api_key))
//
// autocompleteFragment =
// childFragmentManager.findFragmentById(R.id.autoComplete_fragment) as AutocompleteSupportFragment
//
// autocompleteFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.ADDRESS,Place.Field.LAT_LNG))
//
// autocompleteFragment.setOnPlaceSelectedListener(object:PlaceSelectionListener{
// override fun onError(p0: Status) {
// Utils.showToast(requireContext(),p0.toString())
// Log.d("apikeyerror", p0.toString())
// }
//
// override fun onPlaceSelected(place: Place) {
//// val add = place.address
//// val id = place.id
// val latlng = place.latLng
// zoomOnMap(latlng)
// }
//
// })

// BEHAVIOUR OF BOTTOM SHEET
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        initialPeekHeight =
            resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._30sdp) // Set your initial peek height here


// SET THE PEEK HEIGHT OF BOTTOM SHEET
        bottomSheetBehavior.peekHeight = initialPeekHeight


//SHOWING MAP
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


// REQUEST FOR LOCATION


        // Use Dexter to request permissions
        Dexter.withActivity(requireActivity())
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        // All permissions granted, proceed with location-related tasks
                        buildLocationRequest()
                        buildLocationCallBack()
                        fusedLocationProviderClient =
                            LocationServices.getFusedLocationProviderClient(requireActivity())
                        settingGeoFire()
                        // Initialize the map
                        val mapFragment =
                            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
                        mapFragment.getMapAsync(this@SearchFragment)
                    } else {
                        // Handle the case where permissions are not granted
                        // You can show a message or take appropriate action here
                        Utils.showToast(
                            requireContext(),
                            "Permissions are required for this feature"
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    // Handle the case where permission rationale should be shown
                    // You can show a message or explanation to the user here
                    // In this case, the rationale is not shown, so you can simply proceed
                    token.continuePermissionRequest()
                }
            }).check()






        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        retrieveNotificationsWithinUserRadius()

        return binding.root
    }

// retrieving Notification of searched place into map

    private fun retrieveNotificationsWithinRadius(location: LatLng) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//clear data from list
                notificationArrayList.clear()
                for (notificationSnapshot in dataSnapshot.children) {
                    val fromLatitude = notificationSnapshot.child("from").child("latitude")
                        .getValue(Double::class.java)
                    val fromLongitude = notificationSnapshot.child("from").child("longitude")
                        .getValue(Double::class.java)
                    val message = notificationSnapshot.child("message").getValue(String::class.java)
                    val toLatitude = notificationSnapshot.child("to").child("latitude")
                        .getValue(Double::class.java)
                    val toLongitude = notificationSnapshot.child("to").child("longitude")
                        .getValue(Double::class.java)
                    val time =
                        notificationSnapshot.child("upload_time").getValue(String::class.java)
                    if (fromLatitude != null && fromLongitude != null && message != null) {
                        val fromLocation = LatLng(fromLatitude, fromLongitude)
                        val to_location = getLocationName(toLatitude!!, toLongitude!!)
                        val distance = calculateDistance(fromLocation, location).toInt()
                        val user = notification_content(
                            null,
                            null,
                            null,
                            null,
                            message,
                            to_location,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            time
                        )

                        Log.d("distance", distance.toString())
                        if (distance <= 1000) {
// Check if the notification is within 1km radius
                            notificationArrayList.add(user!!)

                        }
                    }
                }
// Set the adapter after fetching all notifications
                notificationRecyclerview.adapter =
                    SearchNotificationAdapter(notificationArrayList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to retrieve notifications: ${error.message}")
            }
        })
    }

//clearing list for new area notification

    private fun clearNotificationList() {
        notificationArrayList.clear()
        notificationRecyclerview.removeAllViews()
        notificationRecyclerview.adapter?.notifyDataSetChanged()
    }


    //showing marker in 2km range (2km area)
    private fun retrieveNotificationsWithinUserRadius() {
        if (!isAdded || lastLocation == null) {
            return
        }

        markerdatabaseReference = FirebaseDatabase.getInstance().getReference("notifications")


        markerdatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (notificationSnapshot in dataSnapshot.children) {
                    val fromLatitude = notificationSnapshot.child("from").child("latitude")
                        .getValue(Double::class.java)
                    val fromLongitude = notificationSnapshot.child("from").child("longitude")
                        .getValue(Double::class.java)

                    if (fromLatitude != null && fromLongitude != null && lastLocation != null) {
                        val fromLocation = LatLng(fromLatitude, fromLongitude)
                        val distance = calculateDistance(
                            fromLocation,
                            LatLng(lastLocation!!.latitude,lastLocation!!.longitude)
                        )
                        Log.d("distance", distance.toString())
                        if (distance <= 2000) {
                            addNotificationMarker(fromLocation)
                        }
                    }
                }
// Set the adapter after fetching all notifications
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to retrieve notifications: ${error.message}")
            }
        })

    }


    private fun addNotificationMarker(location: LatLng) {
        val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.hand_up)
// Add marker for the notification at the given location
        mMap?.addMarker(
            MarkerOptions()
                .position(location)
                .icon(customMarkerIcon)
                .title("request")
        )

    }

//changing latitude and longitude into location name with geocoder

    private fun getLocationName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(fragmentContext!!)
        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)!!
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val buildingName = address.featureName ?: ""
                val subBuildingName = address.subThoroughfare ?: ""
                val thoroughfare = address.thoroughfare ?: ""
                val subLocality = address.subLocality ?: ""
                val locality = address.locality ?: ""
                val adminArea = address.adminArea ?: ""
                val countryName = address.countryName ?: ""
                val postalCode = address.postalCode ?: ""

// Concatenate the address components to form the complete address
                val fullAddress = StringBuilder()

                if (buildingName.isNotBlank()) fullAddress.append("$buildingName, ")
// if (subBuildingName.isNotBlank()) fullAddress.append("$subBuildingName, ")
// if (thoroughfare.isNotBlank()) fullAddress.append("$thoroughfare, ")
// if (subLocality.isNotBlank()) fullAddress.append("$subLocality, ")
                if (locality.isNotBlank()) fullAddress.append("$locality, ")
                if (adminArea.isNotBlank()) fullAddress.append("$adminArea")

// if (postalCode.isNotBlank()) fullAddress.append("$postalCode, ")
// if (countryName.isNotBlank()) fullAddress.append(countryName)

                Log.d("nameaddress", fullAddress.toString())
                return fullAddress.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


//MAP RELATED CODE

    // search location
    private fun searchlocation() {
        val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.search_marker)
        val location = binding.searchView.query.toString().trim()
        var addressList: List<Address>? = null

        if (location.isNotEmpty()) {
            val geoCoder = Geocoder(requireContext())
            try {
                addressList = geoCoder.getFromLocationName(location, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val address = addressList!![0]
            val latLng = LatLng(address.latitude, address.longitude)

// Remove the old marker if it exists
            searchMarker?.remove()

            searchMarker = mMap!!.addMarker(
                MarkerOptions().position(latLng).title(location)
                    .icon(customMarkerIcon)
            )
            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

// Clear the notification list and retrieve notifications within radius
            clearNotificationList()
            retrieveNotificationsWithinRadius(latLng)

        }
    }


// private fun zoomOnMap(latLng: LatLng){
// val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng,13.5f)
// mMap?.animateCamera(newLatLngZoom)
// }

    // on map ready
    override fun onMapReady(googlemap: GoogleMap) {
        mMap = googlemap

        val indiaLatLng = LatLng(20.5937, 78.9629) // Coordinates for India
        val zoomLevel = 4.5f // Adjust zoom level as needed

        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(indiaLatLng, zoomLevel))

        if (fusedLocationProviderClient != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission not granted, return
                    return
                }
            }
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.myLooper()
            )
        }

// Check if location permission is granted before enabling "My Location" feature
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission granted, enable "My Location" feature
            mMap?.isMyLocationEnabled = true
        }
    }


    //interacting with firebase
    private fun settingGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("User Location")
        geoFire = GeoFire(myLocationRef)
    }


    // Builds the LocationCallback object for handling location updates
    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (mMap != null) {
                    lastLocation = locationResult.lastLocation!!
                    addUserMarker()
                }
            }
        }
    }


    // add marker
    private fun addUserMarker() {
        val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.curr_user_location)
// Clear previous circles on the map
        mMap?.clear()
// Check if geoFire, lastLocation, and mMap are not null before proceeding
        if (geoFire != null && lastLocation != null && mMap != null) {
            geoFire!!.setLocation(
                auth.currentUser?.uid,
                GeoLocation(lastLocation!!.latitude, lastLocation!!.longitude)
            ) { key, error ->
                if (error != null) {
// Handle error if setting location fails
                    Log.e("TAG", "Error setting location: $error")
                    return@setLocation
                }
// Remove existing marker if present
                if (currentMarker != null) {
                    currentMarker!!.remove()
                }
// Add new marker
                currentMarker = mMap!!.addMarker(
                    MarkerOptions()
                        .position(LatLng(lastLocation!!.latitude, lastLocation!!.longitude))
                        .icon(customMarkerIcon)
                        .title("you")
                )

// Add circle around the user's location
                val circleOptions = CircleOptions()
                    .center(LatLng(lastLocation!!.latitude, lastLocation!!.longitude))
                    .radius(2000.0) // Set the radius here (in meters)
                    .strokeColor(0xFFFFDE59.toInt())
                    .fillColor(0x22808080)
                    .strokeWidth(5.0f)

                mMap!!.addCircle(circleOptions)

// Animate camera to focus on the new marker's position
                mMap!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentMarker!!.position,
                        13.7f
                    )
                )
                retrieveNotificationsWithinUserRadius()
            }

        } else {
// Handle the case where geoFire, lastLocation, or mMap is null
            Log.e("TAG", "geoFire, lastLocation, or mMap is null")
        }
    }


//building location

//caring things -> map change location, the location change seconds,etc

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest!!.interval = 5000
        locationRequest!!.fastestInterval = 3000
        locationRequest!!.smallestDisplacement = 10f
    }

// IMPLEMENTED METHODS NECESSARY

    override fun onLocationLoadSuccess(latLngs: List<MyLatLng>) {
// Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
//clear map and data again
        if (mMap != null) {
            mMap!!.clear()
//Add again user Marker
            addUserMarker()
        }
    }

    override fun onLocationLoadFailed(message: String) {}
    override fun onDataEntered(p0: DataSnapshot?, p1: GeoLocation?) {}
    override fun onDataExited(p0: DataSnapshot?) {}
    override fun onDataMoved(p0: DataSnapshot?, p1: GeoLocation?) {}
    override fun onDataChanged(p0: DataSnapshot?, p1: GeoLocation?) {}
    override fun onGeoQueryReady() {}

    override fun onGeoQueryError(error: DatabaseError?) {}


    private fun retrieveLocationFromSharedPreferences(): LatLng? {
// Retrieve latitude and longitude from SharedPreferences
        val latitude = sharedPreferences.getFloat(KEY_LATITUDE, 0f)
        val longitude = sharedPreferences.getFloat(KEY_LONGITUDE, 0f)

// Check if latitude and longitude are valid
        return if (latitude != 0f && longitude != 0f) {
            LatLng(latitude.toDouble(), longitude.toDouble())
        } else {
            null
        }
    }

    private fun saveLocationInSharedPreferences(location: LatLng) {
// Save latitude and longitude in SharedPreferences
        sharedPreferences.edit().apply {
            putFloat(KEY_LATITUDE, location.latitude.toFloat())
            putFloat(KEY_LONGITUDE, location.longitude.toFloat())
            apply()
        }
    }


    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent) {

        val latlong = LatLng(locationEvent.latitude!!, locationEvent.longitude!!)
        Log.d("newlatlong", "${locationEvent.latitude!!} ${locationEvent.longitude!!}")
        saveLocationInSharedPreferences(latlong)
        retrieveNotificationsWithinUserRadius()
    }




// override fun onStop() {
// fusedLocationProviderClient!!.removeLocationUpdates(locationCallback!!)
// super.onStop()
// }

}