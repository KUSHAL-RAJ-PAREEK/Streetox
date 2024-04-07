package com.streetox.streetox.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.streetox.streetox.R
import com.streetox.streetox.activities.MainActivity
import com.streetox.streetox.activities.UserMainActivity
import kotlin.random.Random


private const val CHANNEL_ID = "streetox_channel"



class FirebaseService : FirebaseMessagingService() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val  fcmToken = dataSnapshot.child("fcmToken").getValue(String::class.java)
                fcmToken?.let { Log.d("toekens", it) }
                notification(message,fcmToken.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })

        message.data.isNotEmpty().let {
            val notificationType = message.data["notificationType"]

            if (notificationType == "alarmNoti") {
                playAlarmSound(this)
            }
        }

    }

    private fun playAlarmSound(context: Context) {
        try {
            // Get the default alarm sound URI
            val alarmSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            // Create a MediaPlayer instance
            val mediaPlayer = MediaPlayer()

            // Set the data source to the alarm sound URI
            mediaPlayer.setDataSource(context, alarmSoundUri)

            // Prepare the MediaPlayer
            mediaPlayer.prepare()

            // Start playing the alarm sound
            mediaPlayer.start()

            // Optionally, you can set looping or handle other configurations
            mediaPlayer.isLooping = true

            // Release the MediaPlayer resources when playback is complete
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.stop()
                mediaPlayer.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun notification(message: RemoteMessage,fcmToken:String){
        val intent: Intent = when (message.data["notificationType"]) {
            "acceptNoti" -> {
                // Create an intent for the first type of notification
                Intent(this, MainActivity::class.java).apply {
                    putExtra("fromNotification", true)
                    putExtra("fcmToken", fcmToken)
                    putExtra("notificationId", message.data["notificationId"])
                    putExtra("UID",auth.currentUser!!.uid)
                }
            }

            "areaNoti" -> {
                // Create an intent for the second type of notification
                Intent(this, MainActivity::class.java).apply {
                    putExtra("AreaNotification", true)
                    putExtra("notificationId", message.data["notificationId"])
                    putExtra("fcmToken", fcmToken)
                }
            }
            "alarmNoti" -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra("AlarmNotification", true)
                    putExtra("notificationId", message.data["notificationId"])
                    putExtra("fcmToken", fcmToken)
                }
            }
            else -> {
                Intent(this, UserMainActivity::class.java).apply {
                    putExtra("AreaNotification", true)
                    putExtra("notificationId", message.data["notificationId"])
                    putExtra("fcmToken", fcmToken)

                }
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,
            FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, MainActivity::class.java).apply {
        }
        val stopPendingIntent = PendingIntent.getActivity(this, 1, stopIntent,
            FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder  = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.so_trans_logo)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (message.data["notificationType"] == "alarmNoti") {
            notificationBuilder.addAction(R.drawable.so_trans_logo, "Stop", stopPendingIntent)
                .setAutoCancel(true)
        }

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(message.data["message"])
        notificationBuilder.setStyle(bigTextStyle)

        val notification = notificationBuilder.build()

        notificationManager.notify(notificationID,notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID,channelName,IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }
}