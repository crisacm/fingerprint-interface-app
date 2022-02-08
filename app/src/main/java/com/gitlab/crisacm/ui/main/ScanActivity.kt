package com.gitlab.crisacm.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import asia.kanopi.fingerscan.Fingerprint
import asia.kanopi.fingerscan.Status
import com.gitlab.crisacm.arduino.R
import com.gitlab.crisacm.arduino.databinding.ActivityScanBinding


class ScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanBinding

    private val fingerprint: Fingerprint by lazy { Fingerprint() }

    companion object {
        const val ARG_IMG = "arg_img"
        const val ARG_ERROR_MSG = "arg_error_msg"
        const val ARG_STATUS = "arg_status"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Scanning..."
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                fingerprint.turnOffReader()
                setResult(RESULT_CANCELED)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()

        fingerprint.scan(this, printHandler, updateHandler)
    }

    override fun onStop() {
        super.onStop()

        fingerprint.turnOffReader()
    }

    private var updateHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val status: Int = msg.data.getInt("status")
            var error: String? = null
            val statusTxt = when (status) {
                Status.INITIALISED -> "Setting up reader"
                Status.SCANNER_POWERED_ON -> "Reader powered on"
                Status.READY_TO_SCAN -> "Ready to scan finger"
                Status.FINGER_DETECTED -> "Finger detected"
                Status.RECEIVING_IMAGE -> "Receiving image"
                Status.FINGER_LIFTED -> "Finger has been lifted off reader"
                Status.SCANNER_POWERED_OFF -> "Reader is off"
                Status.SUCCESS -> "Fingerprint successfully captured"
                Status.ERROR -> {
                    error = msg.data.getString("errorMessage")
                    "Error"
                }
                else -> {
                    error = msg.data.getString("errorMessage")
                    status.toString()
                }
            }

            error?.let { binding.textError.text = it } ?: kotlin.run { binding.textError.visibility = View.GONE }
            binding.textStatus.text = statusTxt
        }
    }

    private var printHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val status: Int = msg.data.getInt("status")

            Intent().apply {
                putExtra(ARG_STATUS, status)
                when (status) {
                    Status.SUCCESS -> putExtra(ARG_IMG, msg.data.getByteArray("img")!!)
                    else -> putExtra(ARG_ERROR_MSG, msg.data.getString("errorMessage").toString())
                }
            }.also {
                setResult(RESULT_OK, it)
                finish()
            }
        }
    }
}