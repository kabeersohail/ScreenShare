package com.example.screenshare

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.example.screenshare.databinding.ActivityMainBinding

class MainActivity : Activity() {

    lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

//        val myCanvasView = MyCanvas(this)
//        myCanvasView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//        myCanvasView.contentDescription = getString(R.string.canvasContentDescription)
//        setContentView(myCanvasView)



        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode == 0) {
            true -> {
                val annotationServiceIntent = Intent(this, AnnotationService::class.java)
                startService(annotationServiceIntent)
            }
            false -> {
                Log.d("SOHAIL","nope")
            }
        }

    }

}