package com.example.screenshare.utils

sealed class ServiceState{
    object BindService : ServiceState()
    object StartForeground: ServiceState()
    object EndForeground: ServiceState()
    object UnbindService: ServiceState()
}