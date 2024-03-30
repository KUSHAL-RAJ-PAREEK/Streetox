package com.streetox.streetox.maps

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.streetox.streetox.R
import com.streetox.streetox.Utils
import com.streetox.streetox.databinding.FragmentPolylineCostumerBinding
import org.json.JSONObject

class PolylineCostumerFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentPolylineCostumerBinding
//    private var mGoogleMap: GoogleMap? = null
//    private var deliveryMarker: Marker? = null
//    private lateinit var iGoogleAPI: IGoogleAPI
//    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPolylineCostumerBinding.inflate(layoutInflater)
//
//        iGoogleAPI = RetrofitGoogleAPIClient.instance!!.create(IGoogleAPI::class.java)
//        val mapFragment =
//            childFragmentManager.findFragmentById(R.id.polyline_map_costumer_Fragment) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
        return binding.root
    }
//
    override fun onMapReady(googleMap: GoogleMap) {
//        mGoogleMap = googleMap
//
//        mGoogleMap!!.uiSettings.isZoomControlsEnabled = true
//        try {
//            val success = mGoogleMap!!.setMapStyle(
//                MapStyleOptions.loadRawResourceStyle(
//                    requireContext(),
//                    R.raw.streetox_dark_with_label
//                )
//            )
//            if (!success) {
//                Log.d("polymapcostmer", "Failed to load map style")
//            }
//        } catch (ex: Resources.NotFoundException) {
//            Log.d("polymapcostmer", "Not found json string for map style")
//        }
//
//        drawRoutes()
    }

    //private fun drawRoutes() {
//        val locationDelivery = LatLng(26.7660, 75.8536)
//        val locationCustomer = LatLng(26.7694, 75.8501)
//
//        // Add marker for delivery location
//        mGoogleMap?.addMarker(
//            MarkerOptions()
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.search_marker))
//                .title("Shipper")
//                .snippet("I am here")
//                .position(locationDelivery)
//        )
//
//        // Add marker for customer location
//        val height = 80
//        val width = 80
//        val bitMapDrawable =
//            ContextCompat.getDrawable(requireContext(), R.drawable.search_location_marker) as BitmapDrawable
//        val resized =
//            Bitmap.createScaledBitmap(bitMapDrawable.bitmap, width, height, false) as Bitmap
//
//        deliveryMarker = mGoogleMap?.addMarker(
//            MarkerOptions()
//                .icon(BitmapDescriptorFactory.fromBitmap(resized))
//                .title("Hello")
//                .snippet("Hello again")
//                .position(locationCustomer)
//        )
//
//        mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(locationCustomer, 18.0f))
//
//        // Draw route
//        val from = "26.7694,75.8501"
//        val to = "26.7660,75.8536"
//
//        compositeDisposable.add(
//            iGoogleAPI.getDirections(
//                "driving",
//                "less_driving",
//                from,
//                to,
//                getString(R.string.google_map_api_key)
//            )!!
//                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
//                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
//                .subscribe({ s ->
//                    try {
//                        val jsonObject = JSONObject(s)
//                        val jsonArray = jsonObject.getJSONArray("routes")
//                        for (i in 0 until jsonArray.length()) {
//                            val route = jsonArray.getJSONObject(i)
//                            val poly = route.getJSONObject("overview_polyline")
//                            val polyline = poly.getString("points")
//                            val polylineList = decodePoly(polyline)
//
//                            val polylineOptions = PolylineOptions()
//                                .color(Color.RED)
//                                .width(12.0f)
//                                .addAll(polylineList)
//
//                            mGoogleMap?.addPolyline(polylineOptions)
//                        }
//                    } catch (e: Exception) {
//                        Log.e("PolylineCostumers ", "Exception: ${e.message}")
//                    }
//                }, { throwable ->
//                    Utils.showToast(requireContext(), throwable.message.toString())
//                })
//        )
    }
//
//    private fun decodePoly(encoded: String): List<LatLng> {
//        val poly: MutableList<LatLng> = ArrayList()
//        var index = 0
//        val len = encoded.length
//        var lat = 0
//        var lng = 0
//        while (index < len) {
//            var b: Int
//            var shift = 0
//            var result = 0
//            do {
//                b = encoded[index++].toInt() - 63
//                result = result or (b and 0x1f) shl shift
//                shift += 5
//            } while (b >= 0x20)
//            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
//            lat += dlat
//            shift = 0
//            result = 0
//            do {
//                b = encoded[index++].toInt() - 63
//                result = result or (b and 0x1f) shl shift
//                shift += 5
//            } while (b >= 0x20)
//            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
//            lng += dlng
//            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
//            poly.add(p)
//        }
//        return poly
//    }
//}
