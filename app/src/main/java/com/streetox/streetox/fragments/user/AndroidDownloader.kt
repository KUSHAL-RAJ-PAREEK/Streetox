package com.streetox.streetox.fragments.user

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import com.squareup.picasso.Downloader
import okhttp3.Request
import okhttp3.Response

class AndroidDownloader (private val context: Context):com.streetox.streetox.Interfaces.Downloader{

    private val downloadManager = context!!.getSystemService(DownloadManager::class.java)

    override fun downloadFile(url: String): Long {
        val request = DownloadManager.Request(url!!.toUri())
            .setMimeType("image/jpg")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle("streetox.jpeg")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"image.jpg")

       return downloadManager.enqueue(request)
    }

}