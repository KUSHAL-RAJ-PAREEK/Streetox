package com.streetox.streetox.viewmodels.Stateviewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StateDate : ViewModel() {
    private val _date= MutableLiveData<String>()
    val date: LiveData<String>
        get() = _date

    fun setDate(need: String) {
        _date.value = need
    }
}