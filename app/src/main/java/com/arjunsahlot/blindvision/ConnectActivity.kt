package com.arjunsahlot.blindvision

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class ConnectActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        val ipAddressEditText = findViewById<EditText>(R.id.ip_address)
        val portNumberEditText = findViewById<EditText>(R.id.port_number)
        val connectButton = findViewById<Button>(R.id.connect_button)
        val errorMessage = findViewById<TextView>(R.id.error_message)

        connectButton.setOnClickListener {
            val ipAddress = ipAddressEditText.text.toString()
            val portNumber = portNumberEditText.text.toString()
            val fullAddress = "http://$ipAddress:$portNumber"

            try {
                // Attempt to connect to the WebSocket
                val socket = IO.socket(fullAddress)
                socket.on(Socket.EVENT_CONNECT, Emitter.Listener {
                    // If connected successfully, start the main activity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("address", fullAddress)
                    startActivity(intent)
                    finish()
                }).on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener {
                    // If an error occurs, display a message
                    runOnUiThread {
                        errorMessage.visibility = View.VISIBLE
                        errorMessage.text = "Could not connect to $fullAddress. Please check the address and try again."
                    }
                })
                socket.connect()
            } catch (e: Exception) {
                errorMessage.visibility = View.VISIBLE
                errorMessage.text = "An error occurred: ${e.message}. Please check the address and try again."
            }
        }
    }
}
