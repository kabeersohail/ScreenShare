package com.example.screenshare.results

import com.twilio.video.TwilioException

sealed class RoomConnectionResult {
    object Success : RoomConnectionResult(){
        object RemoteUserJoined: RoomConnectionResult()
    }
    class Failure(val twilioException: TwilioException?): RoomConnectionResult()
}
