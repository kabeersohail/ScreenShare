package com.example.screenshare.results

import com.twilio.video.RemoteAudioTrack
import com.twilio.video.RemoteDataTrack
import com.twilio.video.RemoteVideoTrack

sealed class RemoteTrack {
    class VideoTrack(val remoteVideoTrack: RemoteVideoTrack) : RemoteTrack()
    class AudioTrack(val remoteAudioTrack: RemoteAudioTrack): RemoteTrack()
    class DataTrack(val remoteDataTrack: RemoteDataTrack?): RemoteTrack()
}