package com.example.screenshare.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.accesstoken.AccessTokenGenerator
import com.example.accesstoken.utils.ProfileData
import com.example.screenshare.R
import com.example.screenshare.databinding.FragmentViewRemoteScreenBinding
import com.example.screenshare.listeners.RemoteParticipantListener
import com.example.screenshare.listeners.RoomListener
import com.example.screenshare.utils.Constants
import com.example.screenshare.results.RoomConnectionResult
import com.twilio.video.*

class ViewRemoteScreenFragment : Fragment() {

    lateinit var binding: FragmentViewRemoteScreenBinding
    lateinit var room: Room
    lateinit var remoteScreen: VideoView

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

        val connectionOptions: ConnectOptions = ConnectOptions.Builder(AccessTokenGenerator().getToken(profileData))
            .roomName(Constants.ROOM_NAME)
            .build()

        room = Video.connect(requireContext(), connectionOptions, RoomListener { roomConnectionResult ->
            when(roomConnectionResult){
                is RoomConnectionResult.Failure -> Toast.makeText(requireContext(), "${roomConnectionResult.twilioException?.message}", Toast.LENGTH_SHORT).show()
                RoomConnectionResult.Success.RemoteUserJoined -> {
                    val remoteParticipants: List<RemoteParticipant> = room.remoteParticipants
                    remoteParticipants.forEach { remoteParticipant ->
                        remoteParticipant.setListener(RemoteParticipantListener { remoteVideoTrack, message ->
                            Toast.makeText(requireContext(), message,Toast.LENGTH_SHORT).show()
                            remoteVideoTrack?.addSink(remoteScreen)
                        })
                    }
                }
                RoomConnectionResult.Success -> {
                    Toast.makeText(requireContext(), getString(R.string.connected), Toast.LENGTH_SHORT).show()
                }
            }
        })

    }

}