package com.example.screenshare.listeners

import android.util.Log
import com.example.screenshare.results.Track
import com.example.screenshare.utils.TAG
import com.example.screenshare.results.VideoTrackPublishResult
import com.twilio.video.*

class LocalParticipantListener(private val callback: (VideoTrackPublishResult)->Unit): LocalParticipant.Listener {
    override fun onAudioTrackPublished(
        localParticipant: LocalParticipant,
        localAudioTrackPublication: LocalAudioTrackPublication
    ) {
        Log.d(TAG,"Audio track published")
    }

    override fun onAudioTrackPublicationFailed(
        localParticipant: LocalParticipant,
        localAudioTrack: LocalAudioTrack,
        twilioException: TwilioException
    ) {
        Log.d(TAG,"Audio track publish failed")
    }

    override fun onVideoTrackPublished(
        localParticipant: LocalParticipant,
        localVideoTrackPublication: LocalVideoTrackPublication
    ) {
        callback.invoke(VideoTrackPublishResult.Success(Track.VideoTrack))
    }

    override fun onVideoTrackPublicationFailed(
        localParticipant: LocalParticipant,
        localVideoTrack: LocalVideoTrack,
        twilioException: TwilioException
    ) {
        callback.invoke(VideoTrackPublishResult.Failure(twilioException))
    }

    override fun onDataTrackPublished(
        localParticipant: LocalParticipant,
        localDataTrackPublication: LocalDataTrackPublication
    ) {
        callback.invoke(VideoTrackPublishResult.Success(Track.DataTrack))
    }

    override fun onDataTrackPublicationFailed(
        localParticipant: LocalParticipant,
        localDataTrack: LocalDataTrack,
        twilioException: TwilioException
    ) {
        Log.d(TAG,"Data track publish failed")
    }
}