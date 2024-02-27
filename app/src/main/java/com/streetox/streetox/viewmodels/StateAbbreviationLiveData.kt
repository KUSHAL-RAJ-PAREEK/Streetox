package com.streetox.streetox.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StateAbbreviationLiveData  : ViewModel(){
    private val _abbreviation = MutableLiveData<String>()
    val abbreviation: LiveData<String>
        get() = _abbreviation

    fun setAbbreviation(abbreviation: String) {
        _abbreviation.value = abbreviation
    }
}