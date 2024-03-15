package com.streetox.streetox.fragments.user

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView

import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryDataEventListener
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.streetox.streetox.Listeners.IOnLoadLocationListener

import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentSearchBinding


import com.streetox.streetox.models.MyLatLng
import com.streetox.streetox.models.notification_content
import java.io.IOException
import kotlin.random.Random


class SearchFragment : Fragment(), OnMapReadyCallback, IOnLoadLocationListener,
    GeoQueryDataEventListener {

    private var mMap: GoogleMap? = null
    private lateinit var binding: FragmentSearchBinding
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var currentMarker: Marker? = null
    private lateinit var myLocationRef: DatabaseReference
    private lateinit var dangerousArea: MutableList<LatLng>
    private lateinit var listener: IOnLoadLocationListener
    private lateinit var myCity: DatabaseReference
    private var lastLocation: Location? = null
    private var geoQuery: GeoQuery? = null
    private lateinit var geoFire: GeoFire
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth : FirebaseAuth

    // Global variable to store notification message
    private var notificationMessage: String? = null

    private lateinit var notificationRecyclerview : RecyclerView
    private lateinit var notificationArrayList: ArrayList<notification_content>

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var initialPeekHeight = 0

//    private lateinit var  autocompleteFragment: AutocompleteSupportFragment


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSearchBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()


        databaseReference =  FirebaseDatabase.getInstance().getReference("notifications")



        notificationRecyclerview = binding.notificationRecyclerview

        notificationRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        notificationRecyclerview.setHasFixedSize(true)




        // Set the OnQueryTextListener for the SearchView

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // This method is called when the user submits the query
                // Call the searchLocation() function here
                searchlocation()
                // Return true to indicate that the query has been handled
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // This method is called when the text in the SearchView changes
                // You can perform any live filtering or suggestions here if needed
                return false
            }
        })


        //uses places api rest we are jot using it right now
//        Places.initialize(requireContext(),getString(R.string.google_map_api_key))
//
//        autocompleteFragment =
//            childFragmentManager.findFragmentById(R.id.autoComplete_fragment) as AutocompleteSupportFragment
//
//        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID,Place.Field.ADDRESS,Place.Field.LAT_LNG))
//
//        autocompleteFragment.setOnPlaceSelectedListener(object:PlaceSelectionListener{
//            override fun onError(p0: Status) {
//              Utils.showToast(requireContext(),p0.toString())
//                Log.d("apikeyerror", p0.toString())
//            }
//
//            override fun onPlaceSelected(place: Place) {
////               val add = place.address
////                val id = place.id
//                val latlng = place.latLng
//                zoomOnMap(latlng)
//            }
//
//        })



        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        initialPeekHeight =
            resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._190sdp) // Set your initial peek height here

        // Set the initial peek height for the bottom sheet
        bottomSheetBehavior.peekHeight = initialPeekHeight

        // Listen for keyboard visibility changes
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            adjustBottomSheetPeekHeight()
        }

        //showing map
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        Dexter.withActivity(requireContext() as Activity?)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    buildLocationRequest()
                    buildLocationCallBack()
                    fusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(requireActivity())
                    initArea()
                    settingGeoFire()

                    exampleUpload()

                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Utils.showToast(requireContext(), "You must enable this permission")
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }

            }).check()


        return binding.root
    }


    private val notificationList = mutableListOf<notification_content>()

    private fun retrieveNotificationsWithinRadius(location: LatLng) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (notificationSnapshot in dataSnapshot.children) {
                    val fromLatitude = notificationSnapshot.child("from").child("latitude").getValue(Double::class.java)
                    val fromLongitude = notificationSnapshot.child("from").child("longitude").getValue(Double::class.java)
                    val message = notificationSnapshot.child("message").getValue(String::class.java)

                    if (fromLatitude != null && fromLongitude != null && message != null) {
                        val fromLocation = LatLng(fromLatitude, fromLongitude)
                        val distance = calculateDistance(fromLocation, location)

                        if (distance <= 1000) { // Check if the notification is within 1km radius
                            // Store the message globally
                            notificationMessage = message

                            activity?.runOnUiThread {
                                Utils.showToast(requireContext(), message)
                                sendNotification(message)
                            }

                            val notificationDetails = notification_content(fromLocation, null,message)
                            notificationList.add(notificationDetails)

                            // Process the retrieved notification message as needed
                            Log.d("Notification_message", "Retrieved notification message: $message")
                        }

                        // Create notification_content object
                        val notification = notification_content(fromLocation, null, message)
                        // Process the retrieved notification data as needed
                        println("Notification: $notification")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to retrieve notifications: ${error.message}")
            }
        })
    }



    private fun displayNotifications() {
        for (notification in notificationList) {
            displayNotification(notification)
        }
    }

        private fun displayNotification(notification: notification_content) {

                sendNotification(notification.message!!)
            Utils.showToast(requireContext(), notification.message)

        }


    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        val R = 6371 // Radius of the Earth in kilometers
        val latDistance = Math.toRadians(to.latitude - from.latitude)
        val lonDistance = Math.toRadians(to.longitude - from.longitude)
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + (Math.cos(Math.toRadians(from.latitude)) * Math.cos(Math.toRadians(to.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c * 1000 // Convert to meters
    }


    private fun exampleUpload() {
        val from = LatLng(26.9124, 75.7873) // Replace with actual LatLng values
        val to = LatLng(26.7735871, 75.84249) // ReACplace with actual LatLng values
        val message = "cell from piet market" // Replace with actual message

        val fromLocationName = getLocationName(from.latitude, from.longitude)
        val toLocationName = getLocationName(to.latitude, to.longitude)

        uploadNotificationContent(from, to, message,fromLocationName,toLocationName)

    }

    private fun getLocationName(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext())
        try {
            val addresses: List<Address> = geocoder.getFromLocation (latitude, longitude, 1)!!
            if (addresses.isNotEmpty()) {
                return addresses[0].getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    // Method to upload notification content to Firebase
    private fun uploadNotificationContent(from: LatLng, to: LatLng, message: String?, fromLocationName: String?,toLocationName:String?) {
        val notificationContent = notification_content(from, to, message,toLocationName,fromLocationName)
        val notificationId = auth.currentUser?.uid // Generate unique key
        if (notificationId != null) {
            databaseReference.child(notificationId).setValue(notificationContent)
                .addOnSuccessListener {
                    // Notification content uploaded successfully
                    Log.d("Firebase", "Notification content uploaded successfully")
                }
                .addOnFailureListener { e ->
                    // Failed to upload notification content
                    Log.e("Firebase", "Failed to upload notification content: ${e.message}")
                }
        }
    }


    private fun adjustBottomSheetPeekHeight() {
        val rect = Rect()
        binding.root.getWindowVisibleDisplayFrame(rect)
        val screenHeight = binding.root.height
        val keyboardHeight = screenHeight - rect.bottom

        // Reduce the adjustment to make the bottom sheet move slightly less up
        val adjustmentFactor = 0.6 // Adjust this factor as needed
        val adjustedKeyboardHeight = (keyboardHeight * adjustmentFactor).toInt()

        val newPeekHeight = if (adjustedKeyboardHeight > screenHeight * 0.15) {
            initialPeekHeight + adjustedKeyboardHeight
        } else {
            initialPeekHeight
        }

        bottomSheetBehavior.peekHeight = newPeekHeight

    }

    // search location
    private fun searchlocation() {
        val location = binding.searchView.query.toString().trim()
        var addressList: List<Address>? = null

        if (location != null || location != "") {
            val geoCoder = Geocoder(requireContext())
            try {
                addressList = geoCoder.getFromLocationName(location, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val address = addressList!![0]
            val latLng = LatLng(address.latitude, address.longitude)
            mMap!!.addMarker(MarkerOptions().position(latLng).title(location))
            mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            retrieveNotificationsWithinRadius(latLng)
            displayNotifications()
        }
    }


//    private fun zoomOnMap(latLng: LatLng){
//        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(latLng,13.5f)
//        mMap?.animateCamera(newLatLngZoom)
//    }
//

    override fun onMapReady(googlemap: GoogleMap) {
        mMap = googlemap

        mMap?.isMyLocationEnabled = true

        if (fusedLocationProviderClient != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                )
                    return
            }
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.myLooper()
            )

        }
    }


    //interacting with firebase
    private fun settingGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation")
        geoFire = GeoFire(myLocationRef)
    }


    // firebase data -> listeners
    private fun initArea() {
        myCity = FirebaseDatabase.getInstance()
            .getReference("DangerousArea")
            .child("MyCity")

        listener = this


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
        // Clear previous circles on the map
        mMap?.clear()
        // Check if geoFire, lastLocation, and mMap are not null before proceeding
        if (geoFire != null && lastLocation != null && mMap != null) {
            geoFire!!.setLocation(
                "you",
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
                        .title("you")
                )

                // Add circle around the user's location
                val circleOptions = CircleOptions()
                    .center(LatLng(lastLocation!!.latitude, lastLocation!!.longitude))
                    .radius(50.0) // Set the radius here (in meters)
                    .strokeColor(0xFFFFDE59.toInt())
                    .fillColor(0x22808080)
                    .strokeWidth(5.0f)

                mMap!!.addCircle(circleOptions)

                // Animate camera to focus on the new marker's position
                mMap!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        currentMarker!!.position,
                        16.5f
                    )
                )
                // Create GeoQuery when user enters dangerous location
                geoQuery = geoFire!!.queryAtLocation(
                    GeoLocation(
                        lastLocation!!.latitude,
                        lastLocation!!.longitude
                    ), 0.05
                ) // 0.5 == 500m
                geoQuery!!.addGeoQueryDataEventListener(this)
            }
        } else {
            // Handle the case where geoFire, lastLocation, or mMap is null
            Log.e("TAG", "geoFire, lastLocation, or mMap is null")
        }
    }


    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest!!.interval = 5000
        locationRequest!!.fastestInterval = 3000
        locationRequest!!.smallestDisplacement = 10f
    }


    // implementory methods

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
            //Add circle of Dangerous area

        }

    }

    override fun onLocationLoadFailed(message: String) {
        Utils.showToast(requireContext(), "" + message)
    }

    override fun onDataEntered(p0: DataSnapshot?, p1: GeoLocation?) {
      //  sendNotification("KRP", String.format("%s entered the dangerous area", p0))
    }


    override fun onDataExited(p0: DataSnapshot?) {
    //    sendNotification("KRP", String.format("%s leave the dangerous area", p0))
    }

    override fun onDataMoved(p0: DataSnapshot?, p1: GeoLocation?) {
      //  sendNotification("KRP", String.format("%s move within the dangerous area", p0))
    }

    override fun onDataChanged(p0: DataSnapshot?, p1: GeoLocation?) {

    }

    override fun onGeoQueryReady() {

    }

    override fun onGeoQueryError(error: DatabaseError?) {
        Utils.showToast(requireContext(), "" + error!!.message)
    }


    // send notification code

    private fun sendNotification(message: String) {
        val NOTIFICATION_CHANNEL_ID = "streetox_multiple_location"
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "MyNotification",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            // Configuring notification channel
            notificationChannel.description = "Channel Description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Building notification
        val builder = NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle("streetox")
            .setContentText(message)
            .setAutoCancel(false)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))

        val notification = builder.build()

        // Generating random notification ID
        val notificationId = Random.nextInt()

        // Displaying notification
        notificationManager.notify(notificationId, notification)
    }


//    override fun onStop() {
//        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback!!)
//        super.onStop()
//    }

}