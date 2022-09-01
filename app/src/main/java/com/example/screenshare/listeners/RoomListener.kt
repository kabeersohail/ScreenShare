package com.example.screenshare.listeners

import android.util.Log
import com.example.screenshare.results.RoomEvent
import com.example.screenshare.results.RoomConnectionResult
import com.example.screenshare.utils.TAG
import com.twilio.video.RemoteParticipant
import com.twilio.video.Room
import com.twilio.video.TwilioException

class RoomListener(private val callback: (RoomConnectionResult)->Unit): Room.Listener {
    override fun onConnected(room: Room) {
        callback.invoke(RoomConnectionResult.Success(RoomEvent.Connected))
        Log.d(TAG,"connected")
    }

    override fun onConnectFailure(room: Room, twilioException: TwilioException) {
        Log.d(TAG,"connection failed: $twilioException")
        callback.invoke(RoomConnectionResult.Failure(twilioException))
    }

    override fun onReconnecting(room: Room, twilioException: TwilioException) {
        Log.d(TAG,"Reconnecting")
    }

    override fun onReconnected(room: Room) {
        Log.d(TAG,"Reconnected")
    }

    override fun onDisconnected(room: Room, twilioException: TwilioException?) {
        Log.d(TAG,"Disconnected")
    }

    override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
        Log.d(TAG,"${remoteParticipant.identity} has joined the room")
        callback.invoke(RoomConnectionResult.Success(RoomEvent.RemoteUserJoined))
    }

    override fun onParticipantDisconnected(room: Room, remoteParticipant: RemoteParticipant) {
        Log.d(TAG,"$remoteParticipant disconnected")
    }

    override fun onRecordingStarted(room: Room) {
        Log.d(TAG,"recording started")
    }

    override fun onRecordingStopped(room: Room) {
        Log.d(TAG,"recording stopped")
    }
}