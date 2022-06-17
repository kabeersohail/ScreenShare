package com.example.screenshare.managers

import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.screenshare.services.ScreenCapturerService
import com.example.screenshare.utils.ServiceState


@TargetApi(29)
class ScreenCaptureManager(private val context: Context) {
    lateinit var screenCapturerService: ScreenCapturerService
    private var connection: ServiceConnection

    var currentState: ServiceState = ServiceState.UnbindService

    init {
        val connection: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d("ScreenCaptureManager", "Service connected")

                val binder: ScreenCapturerService.LocalBinder = service as ScreenCapturerService.LocalBinder
                screenCapturerService = binder.getService()
                currentState = ServiceState.BindService

            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d("ScreenCaptureManager", "Service disconnected")
            }
        }

        this.connection = connection

        val intent = Intent(context, ScreenCapturerService::class.java)
        bindService(intent)
    }

    private fun bindService(intent: Intent) = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

    fun startForeground() {
        screenCapturerService.startForeground()
        currentState = ServiceState.StartForeground
    }

    fun endForeground() {
        screenCapturerService.endForeground()
        currentState = ServiceState.EndForeground
    }

    fun unbindService() {
        context.unbindService(connection)
        currentState = ServiceState.UnbindService
    }

}