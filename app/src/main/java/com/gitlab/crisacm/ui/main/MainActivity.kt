package com.gitlab.crisacm.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import asia.kanopi.fingerscan.Status
import com.gitlab.crisacm.arduino.databinding.ActivityMainBinding
import com.gitlab.crisacm.arduino.databinding.DialogScanResultBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.apply {
                title = "Fingerprint Interface"
                setDisplayShowTitleEnabled(true)
            }

            with(binding) {
                cardRead.setOnClickListener {
                    Intent(this@MainActivity, ScanActivity::class.java).also { scanActivityResult.launch(it) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "An exception has occurred!", Toast.LENGTH_SHORT).show()
        }
    }

    private var scanActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val status: Int = it.data?.getIntExtra(ScanActivity.ARG_STATUS, Status.ERROR) ?: Status.ERROR
            val errorMessage: String? = it.data?.getStringExtra(ScanActivity.ARG_ERROR_MSG)

            when (status) {
                Status.SUCCESS -> {
                    it.data?.extras?.getByteArray(ScanActivity.ARG_IMG)?.also { it2 ->
                        getResultAlert(BitmapFactory.decodeByteArray(it2, 0, it2.size), it2.toString()).show()
                    }
                }
                else -> getErrorResultAlert(errorMessage ?: "Invalid").show()
            }
        }
    }

    private fun getResultAlert(bitmap: Bitmap, bytes: String): AlertDialog {
        val dialogView = DialogScanResultBinding.inflate(LayoutInflater.from(this),  null, false)

        dialogView.image.setImageBitmap(bitmap)
        dialogView.textBytes.text = bytes

        return AlertDialog.Builder(this)
            .setTitle("Resultados de la lectura")
            .setView(dialogView.root)
            .setPositiveButton("Ok", null)
            .create()
    }

    private fun getErrorResultAlert(errorMessage: String): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(errorMessage)
            .setPositiveButton("Entiendo", null)
            .create()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}