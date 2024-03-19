package com.streetox.streetox.fragments.user
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.Utils.calculateDistance
import com.streetox.streetox.adapters.InAreaNotificationAdapter
import com.streetox.streetox.databinding.FragmentNotificationBinding
import com.streetox.streetox.models.LocationEvent
import com.streetox.streetox.models.notification_content
import com.streetox.streetox.service.LocationService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var inarearecyclerview: RecyclerView
    private lateinit var inareanotificationlist: ArrayList<notification_content>
    private var fragmentContext: Context? = null

    // SharedPreferences keys
    private val PREFS_NAME = "LocationPrefs"
    private val KEY_LATITUDE = "latitude"
    private val KEY_LONGITUDE = "longitude"

    // SharedPreferences instance
    private lateinit var sharedPreferences: SharedPreferences


    private var service: Intent? = null

    private var userLatitude: Double? = null
    private var userLongitude: Double? = null

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private val backgroundLocation =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {

            }
        }


    private val locationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                it.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
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

                it.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {

                }

            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentNotificationBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("notifications")

        service = Intent(requireContext(), LocationService::class.java)

        inarearecyclerview = binding.notificationRecyclerview
        inarearecyclerview.layoutManager = LinearLayoutManager(requireContext())
        inarearecyclerview.setHasFixedSize(true)
        inareanotificationlist = arrayListOf<notification_content>()

        checkpermissions()


        // Initialize SharedPreferences
        sharedPreferences =
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Other initialization code...

        // Retrieve location from SharedPreferences and call retrieveNotificationsWithinRadius()
//        retrieveLocationFromSharedPreferences()?.let { retrieveNotificationsWithinRadius(it) }
        retrieveNotificationsWithinRadius()
        // DIVIDER FOR RECYCLER VIEW
        val dividerItemDecoration =
            object : DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            ) {
                override fun onDraw(
                    c: Canvas,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val dividerLeft = parent.paddingLeft
                    val dividerRight = parent.width - parent.paddingRight

                    val childCount = parent.childCount
                    for (i in 0 until childCount - 1) { // Iterate over all items except the last one
                        val child = parent.getChildAt(i)
                        val params = child.layoutParams as RecyclerView.LayoutParams

                        val dividerTop = child.bottom + params.bottomMargin
                        val dividerBottom =
                            dividerTop + (drawable?.intrinsicHeight ?: 0)

                        drawable?.setBounds(
                            dividerLeft,
                            dividerTop,
                            dividerRight,
                            dividerBottom
                        )
                        drawable?.draw(c)
                    }
                }
            }

        ResourcesCompat.getDrawable(resources, R.drawable.in_area_divider, null)?.let { drawable ->
            dividerItemDecoration.setDrawable(drawable)
        }

        binding.notificationRecyclerview.addItemDecoration(dividerItemDecoration)

        return binding.root
    }

    private fun retrieveNotificationsWithinRadius() {
        if (!isAdded) {
            return
        }

        val savedLocation = retrieveLocationFromSharedPreferences()
        savedLocation?.let { location ->
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //clear data from list
                    clearNotificationList()
                    for (notificationSnapshot in dataSnapshot.children) {
                        val fromLatitude = notificationSnapshot.child("from").child("latitude")
                            .getValue(Double::class.java)
                        val fromLongitude = notificationSnapshot.child("from").child("longitude")
                            .getValue(Double::class.java)
                        val message =
                            notificationSnapshot.child("message").getValue(String::class.java)
                        val toLatitude = notificationSnapshot.child("to").child("latitude")
                            .getValue(Double::class.java)
                        val toLongitude = notificationSnapshot.child("to").child("longitude")
                            .getValue(Double::class.java)
                        val time = getCurrentTime()

                        if (fromLatitude != null && fromLongitude != null && message != null) {
                            val fromLocation = LatLng(fromLatitude, fromLongitude)
                            val to_location =
                                getLocationName(fragmentContext!!, toLatitude!!, toLongitude!!)
                            val distance = calculateDistance(fromLocation, location).toInt()
                            val user =
                                notification_content(null, null, null,message, to_location, null, time)

                            Log.d("distance", distance.toString())
                            if (distance <= 2000) {
                                // Check if the notification is within 1km radius
                                inareanotificationlist.add(user!!)
                            }
                        }
                    }
                    // Set the adapter after fetching all notifications
                    inarearecyclerview.adapter =
                        InAreaNotificationAdapter(inareanotificationlist)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to retrieve notifications: ${error.message}")
                }
            })
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    private fun checkpermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                // Start the service using startForegroundService() method
                requireContext().startForegroundService(service)
            }
        }
    }


    private fun clearNotificationList() {
        inareanotificationlist.clear()
        inarearecyclerview.removeAllViews()
        inarearecyclerview.adapter?.notifyDataSetChanged()
    }


    private fun getLocationName(context: Context, latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context)
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
                //  if (subBuildingName.isNotBlank()) fullAddress.append("$subBuildingName, ")
                //  if (thoroughfare.isNotBlank()) fullAddress.append("$thoroughfare, ")
                //  if (subLocality.isNotBlank()) fullAddress.append("$subLocality, ")
                if (locality.isNotBlank()) fullAddress.append("$locality, ")
                if (adminArea.isNotBlank()) fullAddress.append("$adminArea")

                //   if (postalCode.isNotBlank()) fullAddress.append("$postalCode, ")
                //   if (countryName.isNotBlank()) fullAddress.append(countryName)

                Log.d("nameaddress", fullAddress.toString())
                return fullAddress.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }


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

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val pattern = "hh:mm a" // Define your desired time pattern here
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return simpleDateFormat.format(calendar.time)
    }


    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent) {

        val latlong = LatLng(locationEvent.latitude!!, locationEvent.longitude!!)
        Log.d("newlatlong", "${locationEvent.latitude!!}   ${locationEvent.longitude!!}")
        saveLocationInSharedPreferences(latlong)
        clearNotificationList() // Clear old list before retrieving new notifications
        retrieveNotificationsWithinRadius()
    }
}
