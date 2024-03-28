package com.streetox.streetox.maps

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dot
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
import com.streetox.streetox.databinding.FragmentMapCostumerBinding

class MapCostumerFragment : Fragment(), OnMapReadyCallback {

    // Declare variables
    private lateinit var binding: FragmentMapCostumerBinding
    private var mGoogleMap: GoogleMap? = null
    private lateinit var userLocationRef: DatabaseReference
    private var userMarker: Marker? = null
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapCostumerBinding.inflate(layoutInflater)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_costumer_Fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        auth = FirebaseAuth.getInstance()
        userLocationRef = FirebaseDatabase.getInstance().getReference("currentLocation").child(auth.currentUser!!.uid)
        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap!!.uiSettings.isZoomControlsEnabled = true

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

        // Fixed locations (example)
        val fixedLocation1 = LatLng(20.5937, 78.9629)
        val fixedLocation2 = LatLng(21.5937, 79.9629)

        userLocationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("current_location")) {
                    val currentLocationSnapshot = dataSnapshot.child("current_location")

                    val latitude = currentLocationSnapshot.child("latitude").value as Double
                    val longitude = currentLocationSnapshot.child("longitude").value as Double

                    // Remove previous polyline
                    mGoogleMap?.clear()

                    val newLocation = LatLng(latitude, longitude)

                    // Update user marker position or create a new one if it doesn't exist

                        val markerOptions = MarkerOptions().position(newLocation).title("User Location")
                        userMarker = mGoogleMap?.addMarker(markerOptions)


                    // Draw polyline passing through three locations
                    drawCurvedArrow(newLocation, fixedLocation1, 5, Color.YELLOW, 5f)

                    // Optionally, move the camera to focus on the user's current location
                    mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15f))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
            }
        })


    }

    private fun drawCurvedArrow(
        markerPosition: LatLng,
        fixedLocation: LatLng,
        curveRadius: Int,
        color: Int,
        lineWidth: Float
    ) {
        val midX = (markerPosition.latitude + fixedLocation.latitude) / 2
        val midY = (markerPosition.longitude + fixedLocation.longitude) / 2

        val xDiff = midX - markerPosition.latitude
        val yDiff = midY - markerPosition.longitude

        val angle = (Math.atan2(yDiff, xDiff) * (180 / Math.PI)) - 90
        val angleRadians = Math.toRadians(angle)
        val pointX = (midX + curveRadius * Math.cos(angleRadians))
        val pointY = (midY + curveRadius * Math.sin(angleRadians))

        val path = Path()
        path.moveTo(markerPosition.latitude.toFloat(), markerPosition.longitude.toFloat())
        path.cubicTo(
            markerPosition.latitude.toFloat(),
            markerPosition.longitude.toFloat(),
            pointX.toFloat(),
            pointY.toFloat(),
            fixedLocation.latitude.toFloat(),
            fixedLocation.longitude.toFloat()
        )

        val points = ArrayList<LatLng>()
        val step = 0.01
        var t = 0.0
        while (t <= 1.0) {
            val x = (1 - t) * (1 - t) * markerPosition.latitude + 2 * (1 - t) * t * pointX + t * t * fixedLocation.latitude
            val y = (1 - t) * (1 - t) * markerPosition.longitude + 2 * (1 - t) * t * pointY + t * t * fixedLocation.longitude
            points.add(LatLng(x, y))
            t += step
        }

        // Create PolylineOptions with a dotted pattern
        val polylineOptions = PolylineOptions()
            .addAll(points)
            .width(lineWidth)
            .color(color)
            .pattern(arrayListOf(Dot(), Gap(10f))) // Adjust the gap value for the dotted pattern

        // Add the polyline to the map
        mGoogleMap?.addPolyline(polylineOptions)
    }

}
