package com.streetox.streetox.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StateNameViewModel: ViewModel() {

    private val _firstName = MutableLiveData<String>()
    val firstName: LiveData<String>
        get() = _firstName

    private val _lastName = MutableLiveData<String>()
    val lastName: LiveData<String>
        get() = _lastName

    fun setUserDetails(firstName: String, lastName: String) {
        _firstName.value = firstName
        _lastName.value = lastName
    }
}