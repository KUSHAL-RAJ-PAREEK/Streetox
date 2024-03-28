package com.streetox.streetox.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.streetox.streetox.R
import com.streetox.streetox.models.LocationEvent
import org.greenrobot.eventbus.EventBus

class LocationService : Service() {

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null

    private var notificationManager: NotificationManager? = null

    private var location: Location?= null

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    companion object{
        const val CHANNEL_ID = "12345"
        const val NOTIFICATION_ID = 12345
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 500).setIntervalMillis(500).setMinUpdateDistanceMeters(2f)
                .build()

        locationCallback = object : LocationCallback(){
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult)
            }
        }

        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(CHANNEL_ID,"locations",NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(notificationChannel)
        }

        startForeground(NOTIFICATION_ID,getNotification())
    }


    @SuppressLint("MissingPermission")
    fun createLocationRequest(){
        try{
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest!!, locationCallback!!,null
            )
        }catch (e:Exception){
            e.printStackTrace()
        }

    }


    private fun removeLocationLocationUpdates(){
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        stopForeground(true)
        stopSelf()
    }


    @SuppressLint("ForegroundServiceType")
    private fun onNewLocation(locationResult: LocationResult) {
        location = locationResult.lastLocation

        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser!!.uid

        val userReference = databaseReference.child("currentLocation").child(userId)
        val locationReference = userReference.child("current_location")

        // Save latitude and longitude to Firebase
        locationReference.child("latitude").setValue(location?.latitude)
        locationReference.child("longitude").setValue(location?.longitude)
        locationReference.child("uid").setValue(userId)

        // Retrieve and save FCM token (assuming it's stored under "fcmToken" node)
        val fcmTokenReference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("fcmToken")
        fcmTokenReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fcmToken = dataSnapshot.getValue(String::class.java)
                locationReference.child("fcm_token").setValue(fcmToken)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("locationService", "Failed to retrieve FCM token: ${databaseError.message}")
            }
        })

        EventBus.getDefault().post(LocationEvent(
            latitude = location?.latitude,
            longitude = location?.longitude
        ))

    }

    fun getNotification(): Notification{
        val notidication = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentTitle("")
            .setContentText("")
            .setOngoing(true)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notidication.setChannelId(CHANNEL_ID)
        }
        return  notidication.build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createLocationRequest()
        return START_STICKY
    }


    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        removeLocationLocationUpdates()
    }
}