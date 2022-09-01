package com.example.screenshare.fragments

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.accesstoken.AccessTokenGenerator
import com.example.accesstoken.utils.ProfileData
import com.example.screenshare.R
import com.example.screenshare.annotations.MyCanvas
import com.example.screenshare.databinding.FragmentViewTwoBinding
import com.example.screenshare.listeners.RemoteParticipantListener
import com.example.screenshare.listeners.RoomListener
import com.example.screenshare.results.RemoteTrack
import com.example.screenshare.results.RoomConnectionResult
import com.example.screenshare.utils.Constants
import com.twilio.video.*
import java.nio.ByteBuffer

class ViewTwoFragment : Fragment() {

    private lateinit var binding: FragmentViewTwoBinding
    private lateinit var room: Room
    private lateinit var remoteParticipants: List<RemoteParticipant>
    private lateinit var localParticipant: LocalParticipant
    private lateinit var canvas: MyCanvas
    lateinit var windowManager: WindowManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectToRoom()
    }

    @RequiresApi(Build.VERSION_CODES.M)
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

                    localParticipant = room.localParticipant ?: throw Exception("Local participant is null")

                    if (!Settings.canDrawOverlays(requireContext())) {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
                        startActivityForResult(intent, 0)
                    } else {
                        setupCanvas()
                    }

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
                        null -> Toast.makeText(requireContext(),"null",Toast.LENGTH_SHORT).show()
                        is RemoteTrack.DataTrack -> {
                            remoteTrack.remoteDataTrack.setListener(object: RemoteDataTrack.Listener {
                                override fun onMessage(
                                    remoteDataTrack: RemoteDataTrack,
                                    messageBuffer: ByteBuffer,
                                ) {
                                    TODO("Not yet implemented")
                                }

                                override fun onMessage(
                                    remoteDataTrack: RemoteDataTrack,
                                    message: String,
                                ) {

                                    val cordString: String = message
                                    val cords: List<Float> = cordString.split(",").map { it.toFloat() }

                                    val x: Float = cords[0]
                                    val y: Float = cords[1]

                                    canvas.motionTouchEventX = x
                                    canvas.motionTouchEventY = y

                                    when(cords[2].toInt()) {
                                        MotionEvent.ACTION_DOWN -> canvas.touchStart()
                                        MotionEvent.ACTION_MOVE -> canvas.touchMove()
                                        MotionEvent.ACTION_UP -> canvas.touchUp()
                                    }
                                }
                            })
                        }
                    }
                }
            })
        }
    }

    private fun setupCanvas() {

        val localDataTrack = LocalDataTrack.create(requireContext()) ?: throw Exception("Data Track Empty")

        localParticipant.publishTrack(localDataTrack)

        canvas = MyCanvas(requireContext(), localDataTrack)
        windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.demo_view, null)

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

        // remove parent if already exist
        if (view.parent != null) {
            windowManager.removeView(view)
        }

        val bottomRightParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val button: Button = view.findViewById(R.id.eraser)
        button.setOnClickListener {
            canvas.eraser()
        }

        val pen: Button = view.findViewById(R.id.pen)
        pen.setOnClickListener {
            canvas.default()

            if (canvas.parent != null) {
                windowManager.removeView(canvas)
            }

            windowManager.addView(canvas, params)

            if (view.parent != null) {
                windowManager.removeView(view)
            }

            windowManager.addView(view, params)
        }

        val clear: Button = view.findViewById(R.id.clearCanvas)
        clear.setOnClickListener {
            if (canvas.parent != null) {
                canvas.clearCanvas()
                windowManager.removeView(canvas)
            }
        }

        windowManager.addView(canvas, params)
        bottomRightParams.gravity = Gravity.BOTTOM or Gravity.END

        windowManager.addView(view, bottomRightParams)
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