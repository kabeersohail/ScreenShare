package com.example.screenshare

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AnnotationService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val view = LayoutInflater.from(this).inflate(R.layout.demo_view, null)

        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER        //Initially view will be added to top-left corner
        params.x = 0
        params.y = 50

        //Add the view to the window
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        windowManager.addView(view, params)

        val canvas = MyCanvas(this)

        val button = view.findViewById<ToggleButton>(R.id.toggle_paint)

        button.setOnCheckedChangeListener { _, isChecked ->
            when(isChecked) {
                true -> {
                    // remove parent if already exist
                    if(view.parent != null){
                        windowManager.removeView(view)
                    }
                    windowManager.addView(canvas, params)
                    windowManager.addView(view, params)
                }
                false -> {
                    if(canvas.parent != null){
                        windowManager.removeView(canvas)
                    }
                }
            }
        }

    }

}