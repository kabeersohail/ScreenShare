package com.example.screenshare.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.accesstoken.AccessTokenGenerator
import com.example.accesstoken.utils.ProfileData
import com.example.screenshare.R
import com.example.screenshare.databinding.FragmentViewRemoteScreenBinding
import com.example.screenshare.http.IWeGuardAPIService
import com.example.screenshare.http.WEHTTPClient
import com.example.screenshare.listeners.RemoteParticipantListener
import com.example.screenshare.listeners.RoomListener
import com.example.screenshare.models.AccessTokenRequest
import com.example.screenshare.results.DataResult
import com.example.screenshare.utils.Constants
import com.example.screenshare.results.RoomConnectionResult
import com.example.screenshare.utils.Constants.X_DEVICE_HEADER_TWILIO
import com.twilio.video.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewRemoteScreenFragment : Fragment() {

    lateinit var binding: FragmentViewRemoteScreenBinding
    lateinit var room: Room
    lateinit var remoteScreen: VideoView
    lateinit var remoteParticipants: List<RemoteParticipant>
    lateinit var accessToken: String
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewRemoteScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        remoteScreen = binding.remoteScreen
        connectToRoom()
    }

    private fun connectToRoom(){

//        val profileData = ProfileData("SKc55675e70622252b2749f4bf76eab051", "jdv9MaxAYhiO4zuwmw1xZVlKPCqJrZQh", "dev")

        val streamId: String = "355972110040483_V8IDQ_WENA-YH12A"

        val deviceId = "355972110040483"
        val policyId = "6299d11852e43918b53a3fa2"
        val token = "b6214f60cf49783082f70384f755bf22b06223c36fc0da716864ca3517ddb44d"

        val accessTokenRequest = AccessTokenRequest()
        accessTokenRequest.roomId = streamId
        accessTokenRequest.identify = "SALMAN"
        accessTokenRequest.policyId = policyId
        
        val request = WEHTTPClient.getHttpClient()!!.create(IWeGuardAPIService::class.java).getTwilioAccessToken(token, X_DEVICE_HEADER_TWILIO, accessTokenRequest)

        request.enqueue(object: Callback<DataResult<Any>> {
            override fun onResponse(
                call: Call<DataResult<Any>>,
                response: Response<DataResult<Any>>,
            ) {
                val dataResult = response.body()
                accessToken = dataResult?.entity.toString()

                val connectOptions: ConnectOptions = ConnectOptions.Builder(accessToken).roomName(streamId).build()

                room = Video.connect(requireContext(), connectOptions, RoomListener { roomConnectionResult ->
                    when(roomConnectionResult){
                        is RoomConnectionResult.Failure -> Toast.makeText(requireContext(), "${roomConnectionResult.twilioException?.message}", Toast.LENGTH_SHORT).show()
                        RoomConnectionResult.Success.RemoteUserJoined -> {
                            Toast.makeText(requireContext(), "Remote user joined", Toast.LENGTH_SHORT).show()
                            doAction()
                        }
                        RoomConnectionResult.Success -> {
                            Toast.makeText(requireContext(), "Connected to room ${room.name}", Toast.LENGTH_SHORT).show()
                            doAction()
                        }
                    }
                })
            }

            override fun onFailure(call: Call<DataResult<Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "request failure",Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun doAction() {
        remoteParticipants = room.remoteParticipants
        remoteParticipants.forEach { remoteParticipant ->
            remoteParticipant.setListener(RemoteParticipantListener { remoteVideoTrack, message ->
//                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                if (message == "unsubscribed from video track of remote participant") {
                    findNavController().popBackStack()
                } else {
                    remoteVideoTrack?.addSink(remoteScreen)
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        room.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        room.disconnect()
    }

}