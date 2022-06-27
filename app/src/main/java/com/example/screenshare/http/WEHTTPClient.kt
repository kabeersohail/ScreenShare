package com.example.screenshare.http

import android.util.Log
import com.example.screenshare.BuildConfig
import com.example.screenshare.utils.TAG
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Factory class for convenient creation of the Api Service interface
 */
object WEHTTPClient : IHTTPClient {

    private var mRetroClient: Retrofit? = null
    private var mBaseURL = URLBuilder.baseUrl +"/"


    override fun getAppJsonRequestBody(payload: String): RequestBody {
        return payload.toRequestBody("application/json".toMediaTypeOrNull())
    }

    override fun getTextRequestBody(payload: String): RequestBody {
        return payload.toRequestBody("application/json".toMediaTypeOrNull())
    }

    override fun getHttpClient(): Retrofit? {
        if (mRetroClient == null) {
            createHTTPClient()
        }
        return mRetroClient
    }

    override fun bindService(service: Any): Any {
        if (mRetroClient == null) {
            createHTTPClient()
        }
        return mRetroClient!!.create(service::class.java)
    }

    fun initialize() {
        if (mRetroClient == null) createHTTPClient()
        else
            Log.d(TAG, "initialize() :: Base URL cannot be null")
    }

    private fun createHTTPClient(): Retrofit? {
        Log.d(TAG, "createHTTPClient() :: Creating Retrofit HTTP client")

        Log.d(TAG,"createHTTPClient() :: Base URL -->$mBaseURL")

        // Logging
        val interceptor = HttpLoggingInterceptor()
        if(BuildConfig.DEBUG)
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        else
            interceptor.level = HttpLoggingInterceptor.Level.NONE
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        mRetroClient = Retrofit.Builder()
            .baseUrl(mBaseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(client)
            .build()
        return mRetroClient
    }
}