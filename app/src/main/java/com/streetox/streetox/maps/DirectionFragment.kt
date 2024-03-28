package com.streetox.streetox.maps

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.streetox.streetox.R
import com.streetox.streetox.databinding.FragmentDirectionBinding
import com.streetox.streetox.databinding.FragmentSearchBinding
import com.streetox.streetox.viewmodels.Stateviewmodels.OrderDetailViewModel


class DirectionFragment : Fragment() , OnMapReadyCallback {

    private var mGoogleMap : GoogleMap?= null
    private lateinit var binding: FragmentDirectionBinding

    private val viewModel: OrderDetailViewModel by activityViewModels()
    // Declare variables to hold received data
    private var fromLatitude: Double = 0.0
    private var fromLongitude: Double = 0.0
    private var toLatitude: Double = 0.0
    private var toLongitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDirectionBinding.inflate(layoutInflater)


        viewModel.toLatitude.observe(viewLifecycleOwner) { tolat ->
            toLatitude = tolat
        }
        viewModel.toLongitude.observe(viewLifecycleOwner) { tolong ->
            toLongitude = tolong
        }
        viewModel.fromLatitude.observe(viewLifecycleOwner) { fromlat ->
fromLatitude = fromlat
        }
        viewModel.fromLongitude.observe(viewLifecycleOwner) { fromlong ->
fromLongitude = fromlong
        }

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.direction_map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_directionFragment_to_orderDetailFragment)
            }

        })




        return binding.root
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        mGoogleMap?.isMyLocationEnabled = true



        arguments?.let { args ->
            fromLatitude = args.getDouble("fromLatitude")
            fromLongitude = args.getDouble("fromLongitude")
            toLatitude = args.getDouble("toLatitude")
            toLongitude = args.getDouble("toLongitude")
        }
        val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.parcel_marker)
        mGoogleMap?.addMarker(MarkerOptions()
            .position(LatLng(fromLatitude, fromLongitude))
            .title("destition")
            .icon(customMarkerIcon))

        val customMarkerIcon2 = BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)
        mGoogleMap?.addMarker(MarkerOptions()
            .position(LatLng(toLatitude, toLongitude))
            .title("destition")
            .icon(customMarkerIcon2))

        // Create bounds builder and include both markers
        val boundsBuilder = LatLngBounds.Builder()
            .include(LatLng(fromLatitude, fromLongitude))
            .include(LatLng(toLatitude, toLongitude))

        // Set camera to focus on bounds with padding
        val padding = 100 // Padding in pixels
        val bounds = boundsBuilder.build()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mGoogleMap?.animateCamera(cameraUpdate)

    }

}