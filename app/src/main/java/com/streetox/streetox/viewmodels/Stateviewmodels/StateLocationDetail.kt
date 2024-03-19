package com.streetox.streetox.viewmodels.Stateviewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StateLocationDetail : ViewModel() {
    private val _locationDetail= MutableLiveData<String>()
    val locationDetail: LiveData<String>
        get() = _locationDetail

    fun setUserLocationDetail(need: String) {
        _locationDetail.value = need
    }
}