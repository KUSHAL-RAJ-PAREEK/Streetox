package com.streetox.streetox

import android.content.Context
import android.widget.Toast
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

object Utils {


    //make toast
    fun showToast(context: Context, message: String){
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show()
    }

    //calculate age
    public fun calculateAgeFromDate(dateOfBirth: String): Int {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy") // Specify the date format pattern
        val dob = LocalDate.parse(dateOfBirth, formatter) // Parse date of birth string to LocalDate
        val currentDate = LocalDate.now() // Get current date
        val period = Period.between(dob, currentDate) // Calculate period between dates
        return period.years // Return years component of the period
    }


   }






