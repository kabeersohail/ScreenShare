package com.example.screenshare.listeners

import android.util.Log
import com.example.screenshare.utils.RoomConnectionResult
import com.example.screenshare.utils.TAG
import com.twilio.video.RemoteParticipant
import com.twilio.video.Room
import com.twilio.video.TwilioException

class RoomListener(private val callback: (RoomConnectionResult)->Unit): Room.Listener {
    override fun onConnected(room: Room) {
        callback.invoke(RoomConnectionResult.Success)
        Log.d(TAG,"connected")
    }

    override fun onConnectFailure(room: Room, twilioException: TwilioException) {
        Log.d(TAG,"connection failed: $twilioException")
        callback.invoke(RoomConnectionResult.Failure(twilioException))
    }

    override fun onReconnecting(room: Room, twilioException: TwilioException) {
        TODO("Not yet implemented")
    }

    override fun onReconnected(room: Room) {
        TODO("Not yet implemented")
    }

    override fun onDisconnected(room: Room, twilioException: TwilioException?) {
        Log.d(TAG,"Disconnected")
    }

    override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
        Log.d(TAG,"${remoteParticipant.identity} has joined the room")
        callback.invoke(RoomConnectionResult.Success.RemoteUserJoined)
    }

    override fun onParticipantDisconnected(room: Room, remoteParticipant: RemoteParticipant) {
        TODO("Not yet implemented")
    }

    override fun onRecordingStarted(room: Room) {
        TODO("Not yet implemented")
    }

    override fun onRecordingStopped(room: Room) {
        TODO("Not yet implemented")
    }
}