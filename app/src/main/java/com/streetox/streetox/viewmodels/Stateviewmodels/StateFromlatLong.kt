package com.streetox.streetox.viewmodels.Stateviewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StateFromlatLong : ViewModel() {
    private val _latitude = MutableLiveData<Double>()
    val latitude: LiveData<Double>
        get() = _latitude

    private val _longitude = MutableLiveData<Double>()
    val longitude: LiveData<Double>
        get() = _longitude

    fun setLatitude(latitudeValue: Double) {
        _latitude.value = latitudeValue
    }

    fun setLongitude(longitudeValue: Double) {
        _longitude.value = longitudeValue
    }
}