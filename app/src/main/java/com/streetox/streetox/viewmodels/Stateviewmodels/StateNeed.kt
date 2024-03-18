package com.streetox.streetox.viewmodels.Stateviewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StateNeed : ViewModel() {
    private val _need= MutableLiveData<String>()
    val need: LiveData<String>
        get() = _need

    fun setUserNeed(need: String) {
        _need.value = need
    }
}