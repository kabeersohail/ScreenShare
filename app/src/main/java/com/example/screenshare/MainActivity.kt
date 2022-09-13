package com.example.screenshare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.screenshare.databinding.ActivityMainBinding
import com.twilio.video.LocalDataTrack

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var localDataTrack: LocalDataTrack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        localDataTrack = LocalDataTrack.create(this) ?: throw Exception("Local data track error")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

}