package com.streetox.streetox.models

class MyLatLng {
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    // Add a no-argument constructor
    constructor()

    // Optionally, you can add a constructor that initializes latitude and longitude
    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }
}
