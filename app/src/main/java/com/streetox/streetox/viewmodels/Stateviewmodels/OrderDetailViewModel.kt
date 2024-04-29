package com.streetox.streetox.viewmodels.Stateviewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OrderDetailViewModel : ViewModel() {


    private val _uid = MutableLiveData<String>()
    val uid: LiveData<String>
        get() = _uid

    private val _notiId = MutableLiveData<String>()
    val notiId: LiveData<String>
        get() = _notiId

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    private val _toLocation = MutableLiveData<String>()
    val toLocation: LiveData<String>
        get() = _toLocation

    private val _fromLocation = MutableLiveData<String>()
    val fromLocation: LiveData<String>
        get() = _fromLocation

    private val _price = MutableLiveData<String>()
    val price: LiveData<String>
        get() = _price

    private val _locationDesc = MutableLiveData<String>()
    val locationDesc: LiveData<String>
        get() = _locationDesc

    private val _detailRequirement = MutableLiveData<String>()
    val detailRequirement: LiveData<String>
        get() = _detailRequirement

    private val _isMed = MutableLiveData<String>()
    val isMed: LiveData<String>
        get() = _isMed

    private val _isPayable = MutableLiveData<String>()
    val isPayable: LiveData<String>
        get() = _isPayable

    private val _fromLatitude = MutableLiveData<Double>()
    val fromLatitude: LiveData<Double>
        get() = _fromLatitude

    private val _fromLongitude = MutableLiveData<Double>()
    val fromLongitude: LiveData<Double>
        get() = _fromLongitude

    private val _toLatitude = MutableLiveData<Double>()
    val toLatitude: LiveData<Double>
        get() = _toLatitude

    private val _toLongitude = MutableLiveData<Double>()
    val toLongitude: LiveData<Double>
        get() = _toLongitude

    private val _fcmToken = MutableLiveData<String>()
    val fcmToken: LiveData<String>
        get() = _fcmToken

    private val _tm = MutableLiveData<String>()
    val tm: LiveData<String>
        get() = _tm

    private val _Dname = MutableLiveData<String>()
    val Dname: LiveData<String>
        get() = _Dname

    private val _Cname = MutableLiveData<String>()
    val Cname: LiveData<String>
        get() = _Cname

    fun setFromLatitude(value: Double) {
        _fromLatitude.value = value
    }

    fun setFromLongitude(value: Double) {
        _fromLongitude.value = value
    }

    fun setToLatitude(value: Double) {
        _toLatitude.value = value
    }

    fun setToLongitude(value: Double) {
        _toLongitude.value = value
    }

    fun setUid(value: String) {
        _uid.value = value
    }

    fun setNotiId(value: String) {
        _notiId.value = value
    }

    fun setMessage(value: String) {
        _message.value = value
    }

    fun setToLocation(value: String) {
        _toLocation.value = value
    }

    fun setFromLocation(value: String) {
        _fromLocation.value = value
    }

    fun setPrice(value: String) {
        _price.value = value
    }

    fun setLocationDesc(value: String) {
        _locationDesc.value = value
    }

    fun setDetailRequirement(value: String) {
        _detailRequirement.value = value
    }

    fun setIsMed(value: String) {
        _isMed.value = value
    }

    fun setIsPayable(value: String) {
        _isPayable.value = value
    }

    fun setFcmToken(value: String) {
        _fcmToken.value = value
    }
    fun setToffeeMoney(value: String) {
        _tm.value = value
    }

    fun setDName(value: String) {
        _Dname.value = value
    }

    fun setCName(value: String) {
        _Cname.value = value
    }

}
