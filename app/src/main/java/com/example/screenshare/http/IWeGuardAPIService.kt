package com.example.screenshare.http

import com.example.screenshare.models.AccessTokenRequest
import com.example.screenshare.results.DataResult
import com.example.screenshare.models.CallHistoryModel
import com.example.screenshare.models.ConfigModel
import com.example.screenshare.models.FirebaseAuthTokenResponse
import retrofit2.Call
import retrofit2.http.*


interface IWeGuardAPIService{


    @GET("/enterprise/rest/weguard/firebase/auth/{uid}")
    fun getFirbaseAuthToken(
            @Header("access_token") token:String,
            @Header("x_device") device : String,
            @Path("uid") udi : String?): Call<DataResult<FirebaseAuthTokenResponse>>
    @POST("/enterprise/rest/wetalk/video/authtoken")
    fun getTwilioAccessToken(
            @Header("access_token") token:String,
            @Header("x_device") device : String,
            @Body tokenRequestBody: AccessTokenRequest
    ): Call<DataResult<Any>>

    @GET("/enterprise/rest/wetalk/history/device/{deviceId}?limit=10000&page=1")
    fun getVideoCallHistory( @Header("access_token") token:String,
                             @Header("x_device") device : String,@Path("deviceId") deviceId : String) : Call<DataResult<CallHistoryModel>>

    @POST("/enterprise/rest/wetalk/history")
    fun saveCallData( @Body historyModel: CallHistoryModel) : Call<DataResult<CallHistoryModel>>

    @GET("/enterprise/rest/rmq-config")
    fun readRmqConfigs(
            @Header("access_token") token:String,
            @Header("x_device") device :String,
            @Query("actCode") actCode: String,
            @Query("prodActCode") prodActCode: String,
            @Query("policyId") policyId: String,
            @Query("deviceId") deviceId: String): Call<DataResult<ConfigModel>>

}
