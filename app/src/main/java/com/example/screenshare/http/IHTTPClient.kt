package com.example.screenshare.http

import okhttp3.RequestBody
import retrofit2.Retrofit

interface IHTTPClient{

    fun bindService(service: Any) : Any
    fun getHttpClient() : Retrofit?
    fun getAppJsonRequestBody(payload : String ) : RequestBody
    fun getTextRequestBody(payload : String) : RequestBody
}