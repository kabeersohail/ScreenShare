package com.example.screenshare.listeners

import com.example.screenshare.utils.VideoTrackPublishResult
import com.twilio.video.*

class LocalParticipantListener(private val callback: (VideoTrackPublishResult)->Unit): LocalParticipant.Listener {
    override fun onAudioTrackPublished(
        localParticipant: LocalParticipant,
        localAudioTrackPublication: LocalAudioTrackPublication
    ) {
        TODO("Not yet implemented")
    }

    override fun onAudioTrackPublicationFailed(
        localParticipant: LocalParticipant,
        localAudioTrack: LocalAudioTrack,
        twilioException: TwilioException
    ) {
        TODO("Not yet implemented")
    }

    override fun onVideoTrackPublished(
        localParticipant: LocalParticipant,
        localVideoTrackPublication: LocalVideoTrackPublication
    ) {
        callback.invoke(VideoTrackPublishResult.Success)
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
        TODO("Not yet implemented")
    }

    override fun onDataTrackPublicationFailed(
        localParticipant: LocalParticipant,
        localDataTrack: LocalDataTrack,
        twilioException: TwilioException
    ) {
        TODO("Not yet implemented")
    }
}