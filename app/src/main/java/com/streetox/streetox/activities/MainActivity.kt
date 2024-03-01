package com.streetox.streetox.activities

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.streetox.streetox.R
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
