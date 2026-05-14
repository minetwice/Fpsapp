package com.fearlauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SetupActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var continueButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        statusText = findViewById(R.id.statusText)
        continueButton = findViewById(R.id.continueButton)

        continueButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            statusText.text = "Ready! You can continue."
            return
        }

        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            statusText.text = "Ready! You can continue."
        } else {
            statusText.text = "Storage permission required. Please grant."
            ActivityCompat.requestPermissions(this, permissions, 100)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            statusText.text = "Ready! You can continue."
        } else {
            statusText.text = "Permission denied. App may not work correctly."
        }
    }
}
