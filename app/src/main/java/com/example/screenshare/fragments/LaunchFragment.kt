package com.example.screenshare.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.screenshare.MyCanvas
import com.example.screenshare.R
import com.example.screenshare.databinding.FragmentLaunchBinding

class LaunchFragment : Fragment() {

    private lateinit var binding: FragmentLaunchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLaunchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("Deprecation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // To hide action bar
        val activity: AppCompatActivity? = activity as AppCompatActivity?
        activity?.supportActionBar?.hide()

        val myCanvasView = MyCanvas(requireContext())
        myCanvasView.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN
        myCanvasView.contentDescription = getString(R.string.canvasContentDescription)
        requireActivity().setContentView(myCanvasView)
    }

}