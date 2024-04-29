package com.streetox.streetox.activities

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.streetox.streetox.R
import com.streetox.streetox.fragments.oxbox.NotificationData
import com.streetox.streetox.fragments.oxbox.PushNotification
import com.streetox.streetox.fragments.oxbox.RetrofitInstance
import com.streetox.streetox.models.request
import com.streetox.streetox.service.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class UserMainActivity : AppCompatActivity(){
    private lateinit var navController: NavController
    private var service: Intent? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var isDialogShown = false

    private lateinit var timer: Timer
    private lateinit var mapRequesterRef: DatabaseReference
    private lateinit var mapDeliveryRef: DatabaseReference
    private var checking = true
    private lateinit var mapDeliveryListener: ValueEventListener




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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        service = Intent(this,LocationService::class.java)

        timer = Timer()

        checkPermissions()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        setupWithNavController(bottomNavigationView,navController)
        bottomNavigationView.itemIconTintList = null


//        if (intent.hasExtra("fromNotification")) {
//            val fcmToken = intent.getStringExtra("fcmToken")
//            val uid = intent.getStringExtra("UID")
//            val notification_id = intent.getStringExtra("notificationId")
//
//            Log.d("fcmTokensss",fcmToken.toString())
//            Log.d("fcmTokensss",uid.toString())
//            showCustomDialogBox(fcmToken!!,uid!!,notification_id!!)
//
//
//        }
//        if (intent.hasExtra("AreaNotification")) {
//            val fcmToken = intent.getStringExtra("fcmToken")
//
//            Log.d("fcmTokensss",fcmToken.toString())
//
//        }

        mapDeliveryRef = FirebaseDatabase.getInstance().reference.child("mapDelivery")

        float_btn_click()
        startBackgroundAcceptedRequest()
        startBackgroundCheck()
        startBackgroundTask()

    }


    private fun startBackgroundTask() {
        // Schedule the task to run every 5 seconds
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                CoroutineScope(Dispatchers.Main).launch {
                    checkReviewRequest()
                }
            }
        }, 0, 5000)

    }


    private fun startBackgroundCheck() {
        // Schedule the task to run every 2 seconds
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Run the check only if checking is true
                if (checking) {
                    CoroutineScope(Dispatchers.Main).launch {
                        checkMapDelivery()
                    }
                }
            }
        }, 0, 1000)
    }

    private fun startBackgroundAcceptedRequest() {

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

                auth.currentUser?.uid?.let { uid ->
                    CoroutineScope(Dispatchers.Main).launch {
                        checkMapRequester(uid)
                    }
                }
            }
        }, 0, 2000)
    }

    private suspend fun checkMapRequester(uid: String) {
        val mapRequestercheck = database.getReference("mapRequester")

        mapRequestercheck.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    updateButtonVisibility(View.VISIBLE)
                } else {
                    updateButtonVisibility(View.GONE)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("YourActivity", "mapRequester onCancelled", databaseError.toException())
            }
        })
    }

    private fun updateButtonVisibility(visibility: Int) {
        val float_button = findViewById<CardView>(R.id.accepted_request)
        // Update button visibility on the main thread
        runOnUiThread {
            float_button.visibility = visibility
        }
    }

    private suspend fun checkMapDelivery() {
        // Retrieve current user's UID
        val uid = auth.currentUser?.uid

        // Check if user's map delivery data exists
        uid?.let { uid ->
            mapDeliveryRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {

                        // Start the TrackingDeliveryActivity

                    startActivity(Intent(this@UserMainActivity, TrackingDeliveryActivity::class.java))
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("YourFragment", "mapDelivery onCancelled", databaseError.toException())
                }
            })
        }
    }

    private suspend fun checkReviewRequest() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val currentUserUid = currentUser.uid
            val underReviewsRef = database.getReference("underReviews")

            underReviewsRef.child(currentUserUid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && !isDialogShown) {
                        val accepterUid = snapshot.child("requesterUid").getValue(String::class.java)
                        val accepterFcmToken = snapshot.child("requesterFcmToken").getValue(String::class.java)
                        val notificationId = snapshot.child("notificationId").getValue(String::class.java)
                        showCustomDialogBox(accepterUid!!, accepterFcmToken!!,notificationId!!, snapshot)
                        isDialogShown = true
                        Log.d("FirebaseData", "Requester UID: $accepterUid")
                        Log.d("FirebaseData", "Requester FCM Token: $accepterFcmToken")
                    } else {
                        Log.d("FirebaseData", "No review request available")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Error fetching data: $error")
                }
            })
        } else {
            Log.d("FirebaseAuth", "User is not authenticated")
        }
    }

    private fun showCustomDialogBox(uid : String,fcmToken:String, notificationId : String,snapshot: DataSnapshot) {
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
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val accepterfcmToken = getUserFcmToken(uid)
                    val request_aceepted = "request accepted by requester"
            sendNotificationsToToken(accepterfcmToken!!,request_aceepted)

            // Delete the snapshot
            snapshot.ref.removeValue()

                    val underreviewNotifications = FirebaseDatabase.getInstance().getReference("underreviewNotifications")
                    underreviewNotifications.child(notificationId).removeValue()


            val accepterUid = auth.currentUser?.uid

                    val database = FirebaseDatabase.getInstance()
                    val ref = database.reference.child("notifications").child(notificationId!!)

                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val notificationDataMap = dataSnapshot.value as? Map<String, Any>

                                val message = notificationDataMap?.get("message") as? String
                                val from = notificationDataMap?.get("from_location") as? String
                                val to = notificationDataMap?.get("to_location") as? String
                                val locationDetail = notificationDataMap?.get("location_desc") as? String
                                val price = notificationDataMap?.get("price") as? String
                                val detailRequrement = notificationDataMap?.get("detail_requrement") as? String
                                val fromLatitude = (notificationDataMap?.get("from") as? Map<String, Any>)?.get("latitude") as? Double
                                val fromLongitude = (notificationDataMap?.get("from") as? Map<String, Any>)?.get("longitude") as? Double
                                val toLatitude = (notificationDataMap?.get("to") as? Map<String, Any>)?.get("latitude") as? Double
                                val toLongitude = (notificationDataMap?.get("to") as? Map<String, Any>)?.get("longitude") as? Double
                                val toffe_money = notificationDataMap?.get("toffee_money") as? String
                                val ismed = notificationDataMap?.get("ismed") as? String
                                val ispayable = notificationDataMap?.get("ispayable") as? String
                                // Create LatLng objects
                                val fromLatLng = LatLng(fromLatitude!!, fromLongitude!!)
                                val toLatLng = LatLng(toLatitude!!, toLongitude!!)

                                val mapRequesterRef = database.getReference("mapRequester").child(accepterUid!!).push()
                                val mapDeliveryRef = database.getReference("mapDelivery").child(uid)


                                val dataMapReq = request(
                                    mapRequesterRef.key,
                                    notificationId,
                                    uid,
                                    fromLatLng,
                                    toLatLng,
                                    message,
                                    to,
                                    from,
                                    price,
                                    locationDetail,
                                    detailRequrement,
                                    accepterfcmToken,
                                    toffe_money,
                                    ismed,
                                    ispayable
                                )



                                val dataMapDel = request(
                                    mapRequesterRef.key,
                                    notificationId,
                                    accepterUid,
                                    fromLatLng,
                                    toLatLng,
                                    message,
                                    to,
                                    from,
                                    price,
                                    locationDetail,
                                    detailRequrement,
                                    fcmToken,
                                    toffe_money,
                                    ismed,
                                    ispayable
                                )

                                mapRequesterRef.setValue(dataMapReq)


                                mapDeliveryRef.setValue(dataMapDel)

                        database.reference.child("notifications").child(notificationId!!).removeValue()

                            } else {
                                println("Data does not exist")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle any errors
                            println("Error getting data: ${databaseError.toException()}")
                        }
                    })

                } catch (e: Exception) {
                    println("Error: ${e.message}")
                }
            }


            val oxboxRef = FirebaseDatabase.getInstance().reference.child("oxbox")

            Log.d("Firebase", "Attempting to delete child nodes")

            oxboxRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("Firebase", "onDataChange triggered")
                    val fcmTokens = mutableListOf<String>()

                    dataSnapshot.children.forEach { uidSnapshot ->

                        uidSnapshot.children.forEach { requestIdSnapshot ->

                            val notiId = requestIdSnapshot.child("noti_id").getValue(String::class.java)
                            val fcmToken = requestIdSnapshot.child("fcm_token").getValue(String::class.java)
                            if (notiId == notificationId) {

                                requestIdSnapshot.ref.removeValue()
                                    .addOnSuccessListener {
                                        fcmToken?.let {
                                            fcmTokens.add(it)
                                        }
                                        Log.d("Firebase", "Child node removed successfully")
                                    }
                                    .addOnFailureListener { e ->

                                        Log.e("Firebase", "Error removing child node: ${e.message}", e)
                                    }
                            }
                        }
                    }

                    if (fcmTokens.isNotEmpty()) {
                        fcmTokens.forEach { token ->
                            sendNotificationsToToken(token, "The request in your oxbox has been accepted, thus it was removed from your oxbox.")
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                    Log.e("Firebase", "Error querying database: ${databaseError.message}", databaseError.toException())
                }
            })


        isDialogShown = false
        dialog.dismiss()

            dialog.dismiss()
        }

        cancelBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                  val token = getUserFcmToken(uid)
                        val request_declined = "request declined by requester"
                        sendNotificationsToToken(token!!, request_declined)


                    // Delete the snapshot
                    snapshot.ref.removeValue()

                    val underreviewNotifications = FirebaseDatabase.getInstance().getReference("underreviewNotifications")
                    underreviewNotifications.child(notificationId).removeValue()

                    isDialogShown = false



                    val database = FirebaseDatabase.getInstance()
                    val ref = database.reference.child("notifications").child(notificationId!!)

                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                val notificationDataMap = dataSnapshot.value as? Map<String, Any>

                                val message = notificationDataMap?.get("message") as? String
                                val from = notificationDataMap?.get("from_location") as? String
                                val to = notificationDataMap?.get("to_location") as? String
                                val locationDetail = notificationDataMap?.get("location_desc") as? String
                                val price = notificationDataMap?.get("price") as? String
                                val detailRequrement = notificationDataMap?.get("detail_requrement") as? String
                                val fromLatitude = (notificationDataMap?.get("from") as? Map<String, Any>)?.get("latitude") as? Double
                                val fromLongitude = (notificationDataMap?.get("from") as? Map<String, Any>)?.get("longitude") as? Double
                                val toLatitude = (notificationDataMap?.get("to") as? Map<String, Any>)?.get("latitude") as? Double
                                val toLongitude = (notificationDataMap?.get("to") as? Map<String, Any>)?.get("longitude") as? Double
                                val toffe_money = notificationDataMap?.get("toffee_money") as? String
                                val ismed = notificationDataMap?.get("ismed") as? String
                                val ispayable = notificationDataMap?.get("ispayable") as? String
                                // Create LatLng objects
                                val fromLatLng = LatLng(fromLatitude!!, fromLongitude!!)
                                val toLatLng = LatLng(toLatitude!!, toLongitude!!)

                                val database_order = FirebaseDatabase.getInstance().reference
                                val orderrandomKey = database_order.child("order_complete").child(uid).push().key


                                val cancel_info = request(
                                    orderrandomKey.toString(),
                                    notificationId,
                                    uid,
                                    fromLatLng,
                                    toLatLng,
                                    message,
                                    to,
                                    from,
                                    price,
                                    locationDetail,
                                    detailRequrement,
                                    null,
                                    toffe_money,
                                    ismed,
                                    ispayable
                                )


                                if (orderrandomKey != null) {
                                    // Set the order info with the generated key
                                    val orderInfoRef = database_order.child("order_cancel").child("costumer").child(auth.currentUser!!.uid).child(orderrandomKey)
                                    orderInfoRef.setValue(cancel_info)
                                        .addOnCompleteListener { task ->
                                            Log.d("order", "canceled")                                  }
                                } else {
                                    // Handle the case where the key is null
                                    Log.e("order", "Error generating unique key for order info")
                                }


                                if (orderrandomKey != null) {
                                    // Set the order info with the generated key
                                    val orderInfoRef = database_order.child("order_cancel").child("delivery").child(uid).child(orderrandomKey)
                                    orderInfoRef.setValue(cancel_info)
                                        .addOnCompleteListener { task ->
                                            Log.d("order", "canceled")                                  }
                                } else {
                                    // Handle the case where the key is null
                                    Log.e("order", "Error generating unique key for order info")
                                }

                                database.reference.child("notifications").child(notificationId!!).removeValue()

                            } else {
                                println("Data does not exist")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                            println("Error getting data: ${databaseError.toException()}")
                        }
                    })



                } catch (e: Exception) {
                    Log.e(TAG, "Error in cancel button coroutine: ${e.message}")
                    // Handle the error appropriately
                } finally {
                    // Dismiss the dialog
                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }


    @SuppressLint("SuspiciousIndentation")
    private fun sendNotificationsToToken(fcmToken : String,message : String) {
        // Here you can implement the logic to send notifications to each token
        val title = "streetox"
        val message = message
        val notificationId = intent.getStringExtra("notificationId")
        Log.d("fccmto",fcmToken)
        val notificationData = NotificationData(title, message,"areaNoti",notificationId.toString())
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

    private suspend fun getUserFcmToken(uid: String): String? {

        val userRef = database.getReference("Users").child(uid).child("fcmToken")
        return try {
            userRef.get().await().getValue(String::class.java)
        } catch (e: Exception) {

            null
        }
    }

    private fun float_btn_click(){
        val float_button = findViewById<CardView>(R.id.accepted_request)
        float_button.setOnClickListener {
            startActivity(Intent(this@UserMainActivity, AcceptedRequestActivity::class.java))
        }
    }
    override fun onStart() {
        super.onStart()
        checking = true
    }

    override fun onStop() {
        super.onStop()
        checking = false
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer when the activity is destroyed to prevent memory leaks
        timer.cancel()
    }



}