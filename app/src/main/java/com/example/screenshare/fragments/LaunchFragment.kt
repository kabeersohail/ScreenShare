package com.example.screenshare.fragments

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.accesstoken.AccessTokenGenerator
import com.example.accesstoken.utils.ProfileData
import com.example.screenshare.R
import com.example.screenshare.databinding.FragmentLaunchBinding
import com.example.screenshare.listeners.LocalParticipantListener
import com.example.screenshare.listeners.RemoteParticipantListener
import com.example.screenshare.listeners.RoomListener
import com.example.screenshare.managers.ScreenCaptureManager
import com.example.screenshare.utils.Constants.ROOM_NAME
import com.example.screenshare.results.RoomConnectionResult
import com.example.screenshare.utils.TAG
import com.example.screenshare.results.VideoTrackPublishResult
import com.twilio.video.*
import java.lang.Exception


class LaunchFragment : Fragment() {

    private lateinit var binding: FragmentLaunchBinding
    private lateinit var room: Room
    private lateinit var screenCaptureManager: ScreenCaptureManager
    private lateinit var screenVideoTrack: LocalVideoTrack

    private val screenCapturerListener: ScreenCapturer.Listener = object : ScreenCapturer.Listener{
        override fun onScreenCaptureError(errorDescription: String) {
            throw Exception(errorDescription)
        }

        override fun onFirstFrameAvailable() {
            Log.d(TAG, "First frame available")
        }
    }

    private val onScreenCaptureResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ activityResult ->
        if(activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.let { intent ->
                val screenCapturer = ScreenCapturer(
                    requireContext(),
                    activityResult.resultCode,
                    intent,
                    screenCapturerListener
                )

                publishScreenVideoTrack(screenCapturer)

            } ?: throw Exception("Intent not available")
        } else{
            Toast.makeText(requireContext(),"Screen cast permission not granted",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLaunchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        screenCaptureManager = ScreenCaptureManager(requireContext())

        binding.shareScreen.setOnClickListener {

            if(::room.isInitialized){
                when(room.state) {
                    Room.State.CONNECTED -> Toast.makeText(requireContext(), "Already connected", Toast.LENGTH_SHORT).show()
                    Room.State.DISCONNECTED -> connectToRoom()
                    else -> {}
                }

            } else connectToRoom()
        }

        binding.stopScreenShare.setOnClickListener {
            if(::room.isInitialized){
                room.localParticipant?.unpublishTrack(screenVideoTrack)
                room.disconnect()
            } else {
                Toast.makeText(requireContext(), "Screen sharing is not started yet",Toast.LENGTH_SHORT).show()
            }
        }

        binding.viewRemoteScreen.setOnClickListener {
            it.findNavController().navigate(R.id.action_launchFragment_to_viewRemoteScreenFragment)
        }

    }

    private fun connectToRoom() {

        val profileData = ProfileData("SKc55675e70622252b2749f4bf76eab051", "jdv9MaxAYhiO4zuwmw1xZVlKPCqJrZQh", "salman")

        val connectionOptions: ConnectOptions = ConnectOptions.Builder(AccessTokenGenerator().getToken(profileData))
            .roomName(ROOM_NAME)
            .build()

        room = Video.connect(
            requireContext(),
            connectionOptions,
            RoomListener { roomConnectionResult ->

                when (roomConnectionResult) {
                    is RoomConnectionResult.Success -> {
                        Toast.makeText(requireContext(), getString(R.string.connected), Toast.LENGTH_SHORT).show()

                        // Connected to a room

                        if (Build.VERSION.SDK_INT >= 29) screenCaptureManager.startForeground()

                        // Now request permission to capture screen

                        val mediaProjectionManager: MediaProjectionManager =
                            requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

                        requestScreenCapturePermission(mediaProjectionManager)

                    }
                    is RoomConnectionResult.Failure -> {

                        Toast.makeText(requireContext(), "${roomConnectionResult.twilioException?.message}", Toast.LENGTH_SHORT).show()
                    }
                    RoomConnectionResult.Success.RemoteUserJoined -> {
                        val remoteParticipants: List<RemoteParticipant> = room.remoteParticipants
                        remoteParticipants.forEach { remoteParticipant ->
                            remoteParticipant.setListener(RemoteParticipantListener() { _, _ ->

                            })
                        }
                    }
                }
            })
    }

    private fun publishScreenVideoTrack(screenCapturer: ScreenCapturer) {
        // fetch screen video track
        screenVideoTrack = startScreenCapture(screenCapturer)

        // publish the video track
        val localParticipant: LocalParticipant =
            room.localParticipant ?: throw Exception("No local participants found")
        localParticipant.publishTrack(screenVideoTrack)

        localParticipant.setListener(LocalParticipantListener { videoTrackPublishResult ->
            when (videoTrackPublishResult) {
                is VideoTrackPublishResult.Failure -> throw Exception(videoTrackPublishResult.twilioException.message)
                VideoTrackPublishResult.Success -> {
                    Toast.makeText(requireContext(), "Video track published successfully", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun requestScreenCapturePermission(mediaProjectionManager: MediaProjectionManager) =
        onScreenCaptureResult.launch(mediaProjectionManager.createScreenCaptureIntent())

    private fun startScreenCapture(screenCapturer: ScreenCapturer): LocalVideoTrack {
        return LocalVideoTrack.create(requireContext(), true, screenCapturer) ?: throw Exception("Unable to access LocalVideoTrack")
    }

    override fun onDestroy() {
        super.onDestroy()
        screenCaptureManager.endForeground()
        room.disconnect()

        if (Build.VERSION.SDK_INT >= 29) {
            screenCaptureManager.unbindService()
        }

        screenVideoTrack.release()
    }

}