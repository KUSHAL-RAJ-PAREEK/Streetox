package com.streetox.streetox.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.streetox.streetox.R
import com.streetox.streetox.fragments.oxbox.NotificationData
import com.streetox.streetox.fragments.oxbox.PushNotification
import com.streetox.streetox.fragments.oxbox.RetrofitInstance
import com.streetox.streetox.fragments.user.NotificationFragment
import com.streetox.streetox.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserMainActivity : AppCompatActivity(){
    private lateinit var navController: NavController
    private var service: Intent? = null

    private val backgroundLocation = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it){

        }
    }

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        when{
            it.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)->{

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    if(ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION )!= PackageManager.PERMISSION_GRANTED){
                        backgroundLocation.launch(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }
            }
            it.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)->{}

        }
    }

    private val TAG = "UserActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)


        service = Intent(this,LocationService::class.java)

        checkPermissions()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        setupWithNavController(bottomNavigationView,navController)
        bottomNavigationView.itemIconTintList = null

        if (intent.hasExtra("fromNotification")) {
            val fcmToken = intent.getStringExtra("fcmToken")
            Log.d("fcmTokensss",fcmToken.toString())

            showCustomDialogBox(fcmToken!!)

        }
        if (intent.hasExtra("AreaNotification")) {
            val fcmToken = intent.getStringExtra("fcmToken")
            Log.d("fcmTokensss",fcmToken.toString())

        }




    }

    private fun showCustomDialogBox(fcmToken:String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.fragment_notification_dailogragment)

        // Make the dialog modal
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
        }

        val acceptBtn = dialog.findViewById<Button>(R.id.accept_btn)
        val cancelBtn = dialog.findViewById<Button>(R.id.cancel_btn)

        acceptBtn.setOnClickListener {

            val request_aceepted = "request accepted by requester"
            sendNotificationsToToken(fcmToken,request_aceepted)



            dialog.dismiss()
        }

        cancelBtn.setOnClickListener {

            val request_declined = "request declined by requester"
            sendNotificationsToToken(fcmToken,request_declined)

            dialog.dismiss()
        }

        dialog.show()
    }


    @SuppressLint("SuspiciousIndentation")
    private fun sendNotificationsToToken(fcmToken : String,message : String) {
        // Here you can implement the logic to send notifications to each token
        val title = "streetox"
        val message = message
        Log.d("fccmto",fcmToken)
        val notificationData = NotificationData(title, message,"areaNoti")
        val pushNotification = PushNotification(notificationData, fcmToken)
        sendNotification(pushNotification)


    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful){
                Log.d(TAG,"Response : ${Gson().toJson(response)}")
            }else{
                Log.d(TAG,response.errorBody().toString())
            }
        }catch (e : Exception){
            Log.d(TAG,e.toString())
        }
    }

    fun checkPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED){
                locationPermission.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }else{
                startService(service)
            }
        }
    }
}