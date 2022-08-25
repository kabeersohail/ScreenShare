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
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi


class AnnotationService : Service() {
    var mView: HUDView? = null
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Toast.makeText(baseContext, "onCreate", Toast.LENGTH_LONG).show()
        mView = HUDView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT)
        params.gravity = Gravity.END or Gravity.TOP
        params.title = "Load Average"
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        wm.addView(mView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(baseContext, "onDestroy", Toast.LENGTH_LONG).show()
        if (mView != null) {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(mView)
            mView = null
        }
    }
}

class HUDView(context: Context?) : ViewGroup(context) {
    private val mLoadPaint: Paint
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawText("Hello World", 5F, 15F, mLoadPaint)
    }

    override fun onLayout(arg0: Boolean, arg1: Int, arg2: Int, arg3: Int, arg4: Int) {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //return super.onTouchEvent(event);
        Log.d("SOHAIL","on touch event")
        return true
    }

    init {
        Toast.makeText(getContext(), "HUDView", Toast.LENGTH_LONG).show()
        mLoadPaint = Paint()
        mLoadPaint.isAntiAlias = true
        mLoadPaint.textSize = 10F
        mLoadPaint.setARGB(255, 255, 0, 0)
    }
}