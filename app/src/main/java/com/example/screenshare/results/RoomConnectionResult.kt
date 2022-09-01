package com.example.screenshare.results

import com.twilio.video.TwilioException

sealed class RoomConnectionResult {
    class Success(val event: RoomEvent) : RoomConnectionResult()
    class Failure(val twilioException: TwilioException?): RoomConnectionResult()
}

sealed class RoomEvent {
    object Connected: RoomEvent()
    object RemoteUserJoined: RoomEvent()
}
