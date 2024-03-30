package com.streetox.streetox.maps

import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.databinding.ActivityMapCostumerBinding

class MapCostumerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapCostumerBinding
    private var mGoogleMap: GoogleMap? = null
    private lateinit var userLocationRef: DatabaseReference
    private var userMarker: Marker? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapCostumerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_costumer_Fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        auth = FirebaseAuth.getInstance()
        userLocationRef = FirebaseDatabase.getInstance().getReference("currentLocation")
            .child(auth.currentUser!!.uid)


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        try {
            val success = mGoogleMap!!.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                   this@MapCostumerActivity,
                    R.raw.streetox_light_with_label
                )
            )
            if (!success) {
                Log.d("polymapcostmer", "Failed to load map style")
            }
        } catch (ex: Resources.NotFoundException) {
            Log.d("polymapcostmer", "Not found json string for map style")
        }

        val fixedLocation = LatLng(20.5937, 78.9629)

        userLocationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("current_location")) {
                    val currentLocationSnapshot = dataSnapshot.child("current_location")

                    val latitude = currentLocationSnapshot.child("latitude").value as Double
                    val longitude = currentLocationSnapshot.child("longitude").value as Double

                    // Remove previous polyline
                    mGoogleMap?.clear()

                    val newLocation = LatLng(latitude, longitude)

                    val myCustomColor = ContextCompat.getColor(this@MapCostumerActivity, R.color.cardview_tracking)


//                    val markerOptions = MarkerOptions().position(newLocation).title("User Location")
//                    userMarker = mGoogleMap?.addMarker(markerOptions)


                    if (userMarker == null) {
                        val markerOptions = MarkerOptions().position(newLocation).title("User Location")
                        userMarker = mGoogleMap?.addMarker(markerOptions)
                    } else {

                        changePositionSmoothly(userMarker, newLocation)
                    }

                    drawStraightLine(newLocation, fixedLocation, myCustomColor, 5f)

                    mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15f))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })


    }

    private fun drawStraightLine(
        markerPosition: LatLng,
        fixedLocation: LatLng,
        color: Int,
        lineWidth: Float
    ) {
        val points = listOf(markerPosition, fixedLocation)

        // Create PolylineOptions for the dashed line
        val polylineOptions = PolylineOptions()
            .addAll(points)
            .width(lineWidth)
            .color(color)
            .pattern(listOf(Dash(20f), Gap(10f))) // Set the pattern for the dashed line

        // Add the polyline to the map
        mGoogleMap?.addPolyline(polylineOptions)
    }


    fun changePositionSmoothly(marker: Marker?, newLatLng: LatLng) {
        if (marker == null) {
            return
        }

        val markerOptions = MarkerOptions().position(newLatLng).title("User Location")
        userMarker = mGoogleMap?.addMarker(markerOptions)

        val animation = ValueAnimator.ofFloat(0f, 100f)
        var previousStep = 0f
        val deltaLatitude = newLatLng.latitude - marker.position.latitude
        val deltaLongitude = newLatLng.longitude - marker.position.longitude

        animation.duration = 1500

        animation.addUpdateListener { animation ->
            val deltaStep = animation.animatedValue as Float - previousStep
            previousStep = animation.animatedValue as Float
            marker.position = LatLng(marker.position.latitude + deltaLatitude * deltaStep * 1 / 100, marker.position.longitude + deltaStep * deltaLongitude * 1 / 100)
        }
        animation.start()
    }



}
