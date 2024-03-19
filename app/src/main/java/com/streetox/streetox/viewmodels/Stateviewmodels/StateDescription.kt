package com.streetox.streetox.viewmodels.Stateviewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StateDescription : ViewModel() {
    private val _description= MutableLiveData<String>()
    val description: LiveData<String>
        get() = _description

    fun setUserDescription(need: String) {
        _description.value = need
    }
}