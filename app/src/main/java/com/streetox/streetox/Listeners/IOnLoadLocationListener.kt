package com.streetox.streetox.Listeners

import com.streetox.streetox.models.MyLatLng

interface IOnLoadLocationListener {
    fun onLocationLoadSuccess(latLngs:List<MyLatLng>)
    fun onLocationLoadFailed(message : String)
}