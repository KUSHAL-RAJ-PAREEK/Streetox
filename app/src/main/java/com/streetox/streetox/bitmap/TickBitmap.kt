package com.streetox.streetox.bitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

fun TickBitmap(fillColor: Int, bitmap: Bitmap): Bitmap {
    val tickBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(tickBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = fillColor

    // Draw tick mark path
    val path = Path()
    path.moveTo(bitmap.width * 0.1f, bitmap.height * 0.5f)
    path.lineTo(bitmap.width * 0.35f, bitmap.height * 0.75f)
    path.lineTo(bitmap.width * 0.9f, bitmap.height * 0.25f)

    // Set stroke properties
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = bitmap.width * 0.1f

    // Draw the tick mark
    canvas.drawPath(path, paint)

    return tickBitmap
}
