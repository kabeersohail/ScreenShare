package com.example.screenshare.http

import com.example.screenshare.BuildConfig
import com.example.screenshare.utils.Constants.DEV_BASE_URL
import com.example.screenshare.utils.Constants.DEV_FLAVOUR
import com.example.screenshare.utils.Constants.PROD_BASE_URL
import com.example.screenshare.utils.Constants.PROD_FLAVOUR
import com.example.screenshare.utils.Constants.STAGING_BASE_URL
import com.example.screenshare.utils.Constants.STAGING_FLAVOUR


/**
 * created by Shashi
 */
object URLBuilder {
    val baseUrl: String
        get() = DEV_BASE_URL
}