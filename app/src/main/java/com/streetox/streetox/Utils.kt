package com.streetox.streetox

import android.content.Context
import android.widget.Toast

object Utils {

    //make toast
    fun showToast(context: Context, message: String){
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
    }
}