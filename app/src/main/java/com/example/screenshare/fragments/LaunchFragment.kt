package com.example.screenshare.fragments

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.accesstoken.AccessTokenGenerator
import com.example.accesstoken.utils.ProfileData
import com.example.screenshare.R
import com.example.screenshare.annotations.MyCanvas
import com.example.screenshare.databinding.FragmentLaunchBinding
import com.example.screenshare.listeners.LocalParticipantListener
import com.example.screenshare.listeners.RemoteParticipantListener
import com.example.screenshare.listeners.RoomListener
import com.example.screenshare.managers.ScreenCaptureManager
import com.example.screenshare.results.RoomConnectionResult
import com.example.screenshare.results.RoomEvent
import com.example.screenshare.results.VideoTrackPublishResult
import com.example.screenshare.utils.Constants.MAX_SHARED_SCREEN_HEIGHT
import com.example.screenshare.utils.Constants.MAX_SHARED_SCREEN_WIDTH
import com.example.screenshare.utils.Constants.ROOM_NAME
import com.example.screenshare.utils.TAG
import com.twilio.video.*


class LaunchFragment : Fragment() {

    private lateinit var binding: FragmentLaunchBinding
    private lateinit var room: Room
    private lateinit var screenCaptureManager: ScreenCaptureManager
    private lateinit var localParticipant: LocalParticipant
    private lateinit var screenVideoTrack: LocalVideoTrack
    private lateinit var localAudioTrack: LocalAudioTrack
    private lateinit var canvas: MyCanvas
    lateinit var windowManager: WindowManager

    private val screenCapturerListener: ScreenCapturer.Listener = object : ScreenCapturer.Listener{
        override fun onScreenCaptureError(errorDescription: String) {
            throw Exception(errorDescription)
        }

        override fun onFirstFrameAvailable() {
            Log.d(TAG, "First frame available")
        }
    }

    private val requestRecordedAudioPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted  ->
        if(isGranted){
            // Add audio track here
            localAudioTrack = LocalAudioTrack.create(requireContext(), true) ?: throw Exception("Audio track not created")
            localParticipant.publishTrack(localAudioTrack)
            Toast.makeText(requireContext(),"Audio track published", Toast.LENGTH_SHORT).show()

            Log.d("SOHAIL",room.sid)
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
        savedInstanceState: Bundle?,
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

        binding.canvas.setOnClickListener {
            canvas = MyCanvas(requireContext())
            windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)

            params.gravity = Gravity.CENTER        //Initially view will be added to top-left corner
            params.x = 0
            params.y = 50

            windowManager.addView(view, params)
        }
    }

    private fun connectToRoom() {

        val profileData = ProfileData("SKc55675e70622252b2749f4bf76eab051", "jdv9MaxAYhiO4zuwmw1xZVlKPCqJrZQh", "salman")

        val videoBandwidthProfileOptions: VideoBandwidthProfileOptions = VideoBandwidthProfileOptions.Builder()
            .videoContentPreferencesMode(VideoContentPreferencesMode.MANUAL)
            .mode(BandwidthProfileMode.PRESENTATION)
            .build()

        val bandwidthProfileOptions = BandwidthProfileOptions(videoBandwidthProfileOptions)

        val connectionOptions: ConnectOptions = ConnectOptions.Builder(AccessTokenGenerator().getToken(profileData))
            .bandwidthProfile(bandwidthProfileOptions)
            .roomName(ROOM_NAME)
            .build()

        room = Video.connect(
            requireContext(),
            connectionOptions,
            RoomListener { roomConnectionResult ->

                when (roomConnectionResult) {
                    is RoomConnectionResult.Success -> {

                        when(roomConnectionResult.event){
                            RoomEvent.Connected -> {
                                Toast.makeText(requireContext(), getString(R.string.connected), Toast.LENGTH_SHORT).show()

                                // Connected to a room

                                if (Build.VERSION.SDK_INT >= 29) screenCaptureManager.startForeground()

                                // Now request permission to capture screen

                                val mediaProjectionManager: MediaProjectionManager =
                                    requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

                                requestScreenCapturePermission(mediaProjectionManager)
                            }
                            RoomEvent.RecordingStarted -> {
                                Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()
                            }
                            RoomEvent.RecordingStopped -> {
                                Toast.makeText(requireContext(), "Recording stopped", Toast.LENGTH_SHORT).show()
                            }
                            RoomEvent.RemoteUserJoined -> {
                                val remoteParticipants: List<RemoteParticipant> = room.remoteParticipants
                                remoteParticipants.forEach { remoteParticipant ->
                                    remoteParticipant.setListener(RemoteParticipantListener { _, _ ->

                                    })
                                }
                            }
                        }

                    }
                    is RoomConnectionResult.Failure -> {

                        Toast.makeText(requireContext(), "${roomConnectionResult.twilioException?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun publishScreenVideoTrack(screenCapturer: ScreenCapturer) {
        // fetch screen video track
        screenVideoTrack = startScreenCapture(screenCapturer)

        val localTrackPublicationOptions = LocalTrackPublicationOptions(TrackPriority.HIGH)

        // publish the video track
        localParticipant =
            room.localParticipant ?: throw Exception("No local participants found")
        localParticipant.publishTrack(screenVideoTrack, localTrackPublicationOptions)

        localParticipant.setListener(LocalParticipantListener { videoTrackPublishResult ->
            when (videoTrackPublishResult) {
                is VideoTrackPublishResult.Failure -> throw Exception(videoTrackPublishResult.twilioException.message)
                VideoTrackPublishResult.Success -> {
                    Toast.makeText(requireContext(), "Video track published successfully", Toast.LENGTH_SHORT).show()

                    // Request run time audio record permission
                    requestRecordedAudioPermissionResult.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        })
    }

    private fun requestScreenCapturePermission(mediaProjectionManager: MediaProjectionManager) =
        onScreenCaptureResult.launch(mediaProjectionManager.createScreenCaptureIntent())

    private fun startScreenCapture(screenCapturer: ScreenCapturer): LocalVideoTrack {

        val metrics = DisplayMetrics()
        getRealScreenSize(requireContext().applicationContext, metrics)

        adjustScreenMetrics(metrics)

        val width = metrics.widthPixels
        val height = metrics.heightPixels

        return LocalVideoTrack.create(requireContext(), true, screenCapturer, VideoFormat(
            VideoDimensions(width, height),
            24
        )) ?: throw Exception("Unable to access LocalVideoTrack")
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

    fun adjustScreenMetrics(metrics: DisplayMetrics): Float {
        val srcWidth = metrics.widthPixels
        // Adjust translated screencast size for phones with high screen resolutions
        if (metrics.widthPixels > MAX_SHARED_SCREEN_WIDTH || metrics.heightPixels > MAX_SHARED_SCREEN_HEIGHT) {
            val widthScale: Float = metrics.widthPixels.toFloat() / MAX_SHARED_SCREEN_WIDTH
            val heightScale: Float = metrics.heightPixels.toFloat() / MAX_SHARED_SCREEN_HEIGHT
            val maxScale = if (widthScale > heightScale) widthScale else heightScale
            metrics.widthPixels /= maxScale.toInt()
            metrics.heightPixels /= maxScale.toInt()
        }
        val videoScale = metrics.widthPixels.toFloat() / srcWidth

        // Workaround against the codec bug: https://stackoverflow.com/questions/36915383/what-does-error-code-1010-in-android-mediacodec-mean
        // Making height and width divisible by 2
        metrics.heightPixels = metrics.heightPixels and 0xFFFE
        metrics.widthPixels = metrics.widthPixels and 0xFFFE
        return videoScale
    }

    private fun getRealScreenSize(context: Context, metrics: DisplayMetrics) {
        val wm = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay

        // This gets correct screen density, but wrong width and height
        display.getMetrics(metrics)
        val screenSize = Point()
        display.getRealSize(screenSize)
        metrics.widthPixels = screenSize.x
        metrics.heightPixels = screenSize.y
    }

}