package com.example.screenshare.fragments

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.accesstoken.AccessTokenGenerator
import com.example.accesstoken.utils.ProfileData
import com.example.screenshare.R
import com.example.screenshare.annotations.MyCanvas
import com.example.screenshare.databinding.FragmentViewOneBinding
import com.example.screenshare.listeners.RemoteParticipantListener
import com.example.screenshare.listeners.RoomListener
import com.example.screenshare.results.RoomConnectionResult
import com.example.screenshare.results.RoomEvent
import com.example.screenshare.utils.Constants.ROOM_NAME
import com.twilio.video.*


class ViewOneFragment : Fragment() {

    private lateinit var binding: FragmentViewOneBinding
    private lateinit var room: Room
    private lateinit var localParticipant: LocalParticipant
    private lateinit var canvas: MyCanvas
    lateinit var windowManager: WindowManager

    companion object {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentViewOneBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewOne.setOnClickListener {
            connectToRoom()
        }

        binding.viewTwo.setOnClickListener {
            it.findNavController().navigate(R.id.action_launchFragment_to_viewRemoteScreenFragment)
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
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

                        when(roomConnectionResult.event) {
                            RoomEvent.Connected -> {
                                Toast.makeText(requireContext(), getString(R.string.connected), Toast.LENGTH_SHORT).show()

                                localParticipant = room.localParticipant ?: throw Exception("Local participant is null")

                                if (!Settings.canDrawOverlays(requireContext())) {
                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
                                    startActivityForResult(intent, 0)
                                } else {
                                    setupCanvas()
                                }

                                // Connected to a room
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

    override fun onDestroy() {
        super.onDestroy()
        room.disconnect()
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

        val eraser: Button = view.findViewById(R.id.eraser)
        eraser.setOnClickListener {
            canvas.eraser()
        }

        val clear: Button = view.findViewById(R.id.clearCanvas)
        clear.setOnClickListener {
            if (canvas.parent != null) {
                canvas.clearCanvas()
                windowManager.removeView(canvas)
            }
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

            windowManager.addView(view, bottomRightParams)
        }

        windowManager.addView(canvas, params)
        bottomRightParams.gravity = Gravity.BOTTOM or Gravity.END

        windowManager.addView(view, bottomRightParams)
    }

    private fun addView(params: WindowManager.LayoutParams) {
        windowManager.addView(canvas, params)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode == 0) {
            true -> {
               setupCanvas()
            }
            false -> {
                Log.d("SOHAIL","nope")
            }
        }

    }

}