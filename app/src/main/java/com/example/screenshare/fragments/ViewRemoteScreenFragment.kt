package com.example.screenshare.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.accesstoken.AccessTokenGenerator
import com.example.accesstoken.utils.ProfileData
import com.example.screenshare.MainActivity
import com.example.screenshare.databinding.FragmentViewRemoteScreenBinding
import com.example.screenshare.listeners.RemoteParticipantListener
import com.example.screenshare.listeners.RoomListener
import com.example.screenshare.results.RemoteTrack
import com.example.screenshare.results.RoomConnectionResult
import com.example.screenshare.utils.Constants
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioSwitch
import com.twilio.video.*
import java.nio.ByteBuffer


class ViewRemoteScreenFragment : Fragment() {

    private lateinit var binding: FragmentViewRemoteScreenBinding
    private lateinit var room: Room
    lateinit var remoteScreen: VideoView
    private lateinit var remoteParticipants: List<RemoteParticipant>
    private lateinit var audioSwitch: AudioSwitch
    private lateinit var localDataTrack: LocalDataTrack

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentViewRemoteScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        localDataTrack = (requireActivity() as MainActivity).localDataTrack

        remoteScreen = binding.remoteScreen
        audioSwitch = AudioSwitch(requireContext().applicationContext)
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
                is RoomConnectionResult.Success -> {
                    doAction()
                }
            }
        })

    }

    private fun doAction() {
        remoteParticipants = room.remoteParticipants
        remoteParticipants.forEach { remoteParticipant ->
            remoteParticipant.setListener(RemoteParticipantListener { remoteTrack, message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                if (message == "unsubscribed from video track of remote participant") {
                    findNavController().popBackStack()
                } else {

                    when(remoteTrack){
                        is RemoteTrack.AudioTrack -> {
                            Toast.makeText(requireContext(), "${remoteTrack.remoteAudioTrack.isPlaybackEnabled}", Toast.LENGTH_SHORT).show()
                            audioSwitch.start { audioDevices, selectedAudioDevice ->
                                audioDevices.find { it is AudioDevice.Speakerphone }?.let { audioSwitch.selectDevice(it) }
                            }

                            audioSwitch.activate()

                        }
                        is RemoteTrack.VideoTrack -> {
                            remoteTrack.remoteVideoTrack.addSink(remoteScreen)
                        }

                        null -> Toast.makeText(requireContext(),"null",Toast.LENGTH_SHORT).show()
                        is RemoteTrack.DataTrack -> {
                            remoteTrack.remoteDataTrack.setListener(object: RemoteDataTrack.Listener {
                                override fun onMessage(
                                    remoteDataTrack: RemoteDataTrack,
                                    messageBuffer: ByteBuffer,
                                ) {}

                                override fun onMessage(
                                    remoteDataTrack: RemoteDataTrack,
                                    message: String,
                                ) {
                                    Log.d("DATA TRACK", message)
                                }

                            })
                        }
                    }
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
        audioSwitch.stop()
    }

}