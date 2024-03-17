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
import android.util.CloseGuard
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.streetox.streetox.R
import com.streetox.streetox.models.LocationEvent
import org.greenrobot.eventbus.EventBus

class LocationService : Service() {

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null

    private var notificationManager: NotificationManager? = null

    private var location: Location?= null

    companion object{
        const val CHANNEL_ID = "12345"
        const val NOTIFICATION_ID = 12345
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500).setIntervalMillis(500).setMinUpdateDistanceMeters(100f)
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

     EventBus.getDefault().post(LocationEvent(
         latitude = location?.latitude,
         longitude = location?.longitude
     ))

        startForeground(NOTIFICATION_ID,getNotification())
    }

    fun getNotification(): Notification{
        val notidication = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("")
            .setContentText("")
            .setContentTitle("")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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