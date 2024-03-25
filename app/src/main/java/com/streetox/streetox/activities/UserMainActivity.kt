package com.streetox.streetox.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.streetox.streetox.R
import com.streetox.streetox.fragments.user.NotificationFragment

class UserMainActivity : AppCompatActivity(){
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)
        setupWithNavController(bottomNavigationView,navController)
        bottomNavigationView.itemIconTintList = null
        if (intent.hasExtra("fromNotification")) {
            val fcmToken = intent.getStringExtra("fcmToken")
            Log.d("useractvity",fcmToken.toString())
            // Show the dialog if the activity was started from a notification
            showCustomDialogBox()
        }
        if (intent.hasExtra("AreaNotification")) {
            val fcmToken = intent.getStringExtra("fcmToken")
            Log.d("useractvity",fcmToken.toString())
            val intent = Intent(this, NotificationFragment::class.java)
            startActivity(intent)
        }
    }

    private fun showCustomDialogBox() {
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
            // Handle logout button click
            // For example, sign out the user and navigate to the main activity

            dialog.dismiss()
        }

        cancelBtn.setOnClickListener {
            // Handle cancel button click
            dialog.dismiss()
        }

        dialog.show()
    }
}