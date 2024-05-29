package com.vonage.tutorial.phonetoappabdul

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var connectionStatusTextView: TextView
    private lateinit var telecomButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val clientManager = ClientManager.getInstance(applicationContext)

        // request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123);
        }

        connectionStatusTextView = findViewById(R.id.connectionStatusTextView)
        telecomButton = findViewById(R.id.telecomButton)
        telecomButton.setOnClickListener { enableTelecomPermission() }

        clientManager.login { status ->
            runOnUiThread {
                if (status == "Connected") {
                    telecomButton.visibility = VISIBLE
                    connectionStatusTextView.text = "Connected"

                    FirebaseMessagingService.requestToken { token ->
                        val storedToken = clientManager.getStoredToken()

                        if (token != storedToken) {
                            clientManager.registerToken(token)
                        }
                    }
                } else {
                    connectionStatusTextView.text = status
                }
            }
        }
    }

    private fun enableTelecomPermission() {
        val intent = Intent()
        intent.setClassName(
            "com.android.server.telecom",
            "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
        )
        startActivity(intent)
    }
}