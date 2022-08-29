package com.example.screenshare

import android.app.Activity
import android.graphics.BlurMaskFilter
import android.graphics.EmbossMaskFilter
import android.graphics.MaskFilter
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.example.screenshare.databinding.ActivityMainBinding


class MainActivity : Activity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var mPaint: Paint
    private lateinit var mEmboss: MaskFilter
    private lateinit var mBlur: MaskFilter

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mPaint = Paint()
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = -0x10000
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = 12f
        mEmboss = EmbossMaskFilter(floatArrayOf(1f, 1f, 1f),
            0.4f, 6f, 3.5f)
        mBlur = BlurMaskFilter(8f, BlurMaskFilter.Blur.NORMAL)

    }

    fun colorChanged(color: Int) {
        mPaint.color = color
    }

}