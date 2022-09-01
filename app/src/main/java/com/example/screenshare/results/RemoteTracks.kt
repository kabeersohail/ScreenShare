package com.example.screenshare.results

import com.twilio.video.RemoteAudioTrack
import com.twilio.video.RemoteDataTrack
import com.twilio.video.RemoteVideoTrack

sealed class RemoteTrack {
    class DataTrack(val remoteDataTrack: RemoteDataTrack): RemoteTrack()
}