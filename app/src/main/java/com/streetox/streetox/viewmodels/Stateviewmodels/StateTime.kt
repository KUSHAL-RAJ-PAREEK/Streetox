package com.streetox.streetox.viewmodels.Stateviewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StateTime : ViewModel() {
    private val _time= MutableLiveData<String>()
    val time: LiveData<String>
        get() = _time

    fun setTime(need: String) {
        _time.value = need
    }
}