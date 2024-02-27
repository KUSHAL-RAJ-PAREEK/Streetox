package com.streetox.streetox.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StateDobViewModel : ViewModel() {
    private val _dateOfBirth = MutableLiveData<String>()
    val dateOfBirth: LiveData<String>
        get() = _dateOfBirth

    fun setDateOfBirth(dob: String) {
        _dateOfBirth.value = dob
    }
}