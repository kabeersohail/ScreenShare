package com.example.screenshare.results

import com.twilio.video.TwilioException

sealed class VideoTrackPublishResult {
    object Success : VideoTrackPublishResult()
    class Failure(val twilioException: TwilioException) : VideoTrackPublishResult()
}