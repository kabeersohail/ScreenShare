package com.example.screenshare.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.accesstoken.AccessTokenGenerator
import com.example.accesstoken.utils.ProfileData
import com.example.screenshare.databinding.FragmentViewRemoteScreenBinding
import com.example.screenshare.listeners.RemoteParticipantListener
import com.example.screenshare.listeners.RoomListener
import com.example.screenshare.results.RoomConnectionResult
import com.example.screenshare.utils.Constants
import com.twilio.video.*
import tvi.webrtc.SurfaceViewRenderer

class ViewRemoteScreenFragment : Fragment() {

    lateinit var binding: FragmentViewRemoteScreenBinding
    lateinit var room: Room
    lateinit var remoteScreen: VideoView
    lateinit var remoteParticipants: List<RemoteParticipant>
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

        val profileData = ProfileData("SKc55675e70622252b2749f4bf76eab051", "jdv9MaxAYhiO4zuwmw1xZVlKPCqJrZQh", "dev")

        val bandwidthProfileOptions = BandwidthProfileOptions(VideoBandwidthProfileOptions.Builder().videoContentPreferencesMode(VideoContentPreferencesMode.MANUAL).build())

        val connectionOptions: ConnectOptions = ConnectOptions.Builder(AccessTokenGenerator().getToken(profileData))
            .bandwidthProfile(bandwidthProfileOptions)
            .roomName(Constants.ROOM_NAME)
            .build()

        room = Video.connect(requireContext(), connectionOptions, RoomListener { roomConnectionResult ->
            when(roomConnectionResult){
                is RoomConnectionResult.Failure -> Toast.makeText(requireContext(), "${roomConnectionResult.twilioException?.message}", Toast.LENGTH_SHORT).show()
                RoomConnectionResult.Success.RemoteUserJoined -> {
                    doAction()
                }
                RoomConnectionResult.Success -> {
                    doAction()
                }
            }
        })

    }

    private fun doAction() {
        remoteParticipants = room.remoteParticipants
        remoteParticipants.forEach { remoteParticipant ->
            remoteParticipant.setListener(RemoteParticipantListener { remoteVideoTrack, message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                if (message == "unsubscribed from video track of remote participant") {
                    findNavController().popBackStack()
                } else {
//                    remoteScreen.videoScaleType = VideoScaleType.ASPECT_FILL
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