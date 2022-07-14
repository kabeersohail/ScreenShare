package com.example.accesstoken

import com.example.accesstoken.utils.Constants.TWILIO_ACCOUNT_SID
import com.example.accesstoken.utils.ProfileData
import com.twilio.jwt.accesstoken.AccessToken
import com.twilio.jwt.accesstoken.VideoGrant


class AccessTokenGenerator {

    fun getToken(profileData: ProfileData): String =
        AccessToken.Builder(TWILIO_ACCOUNT_SID, profileData.apiKey, profileData.apiSecret)
            .identity(profileData.identity)
            .grant(VideoGrant().setRoom("MyRoom"))
            .build().toJwt()
}