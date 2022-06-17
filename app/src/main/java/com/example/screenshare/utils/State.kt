package com.example.screenshare.utils

sealed class State{
    object BindService : State()
    object StartForeground: State()
    object EndForeground: State()
    object UnbindService: State()
}