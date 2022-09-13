package com.example.screenshare.results

import com.twilio.video.TwilioException

sealed class VideoTrackPublishResult {
    class Success(val track: Track) : VideoTrackPublishResult()
    class Failure(val twilioException: TwilioException) : VideoTrackPublishResult()
}

sealed class Track {
    object VideoTrack: Track()
    object DataTrack: Track()
}