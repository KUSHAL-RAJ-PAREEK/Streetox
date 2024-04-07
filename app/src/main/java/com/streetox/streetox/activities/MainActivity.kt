package com.streetox.streetox.activities

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.android.play.integrity.internal.i
import com.razorpay.Checkout
import com.streetox.streetox.R
import com.streetox.streetox.fragments.user.NotificationFragment
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        facebook_login()
        // statusbar changing function
        setStatusBarColor()


        if (intent.hasExtra("fromNotification")) {
            val fcmToken = intent.getStringExtra("fcmToken")
            Log.d("useractvity",fcmToken.toString())

            Handler().postDelayed({

                val intent = Intent(this, UserMainActivity::class.java)
                intent.putExtra("fromNotification", true)
                intent.putExtra("fcmToken", fcmToken)
                startActivity(intent)
                finish() // Finish the current activity to prevent going back to it
            }, 3000)
        }

        if (intent.hasExtra("AreaNotification")) {
            val fcmToken = intent.getStringExtra("fcmToken")

            Handler().postDelayed({
                val bundle = Bundle().apply {
                    putString("fcmToken", fcmToken)
                }
                val notificationFragment = NotificationFragment()
                notificationFragment.arguments = bundle
                // Navigate to the fragment (assuming your fragment container is R.id.fragment_container)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.notiFragment, notificationFragment)
                    .commit()

            }, 3000)
        }


    }

    private fun facebook_login(){
        printKeyHash()
    }

    private fun printKeyHash() {
        try{
            val packageManager = this.packageManager
            val info = packageManager.getPackageInfo("com.streetox.streetox", PackageManager.GET_SIGNATURES)
            for(signatur in info.signatures){
                val md = MessageDigest.getInstance("SHA")
                md.update(signatur.toByteArray())
                Log.e("KEYHASH", Base64.encodeToString(md.digest(),Base64.DEFAULT))
            }
        }
        catch (e: PackageManager.NameNotFoundException){

        }
        catch (e: NoSuchAlgorithmException){

        }
    }
    override fun onStop() {
        super.onStop()
        clearSharedPreferences()
    }

    private fun clearSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    // changing status bar color
    private fun setStatusBarColor() {
        window?.apply {
            val statusBarColors = ContextCompat.getColor(
                applicationContext,
                R.color.streetox_primary_color
            )
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }


    }

}
