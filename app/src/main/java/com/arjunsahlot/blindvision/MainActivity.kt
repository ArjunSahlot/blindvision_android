package com.arjunsahlot.blindvision

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var mSocket: Socket
    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("MainActivity", "Record Audio Permission Not Granted")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }

        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.US
            }
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
            this.packageName)

        val listener = SpeechRecognitionListener()
        speechRecognizer.setRecognitionListener(listener)

        val address = intent.getStringExtra("address")
        mSocket = IO.socket(address)
        mSocket.on(Socket.EVENT_CONNECT, Emitter.Listener {
            Log.d("MainActivity", "connected")
        }).on("tts", Emitter.Listener {
            val message = (it[0] as JSONObject).getString("message")
            Log.d("MainActivity", message)
            speakOut(message)
        }).on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            Log.d("MainActivity", "disconnected")
        })
        mSocket.connect()

        findViewById<Button>(R.id.default_button).setOnClickListener { onPress("default") }
        findViewById<Button>(R.id.risks_button).setOnClickListener { onPress("risks") }
        findViewById<Button>(R.id.camera_button).setOnClickListener { onPress("camera") }
        findViewById<Button>(R.id.refresh_button).setOnClickListener { onPress("refresh") }
        val chatButton = findViewById<TouchListenableButton>(R.id.chat_button)
        chatButton.onTouchDown = { speechRecognizer.startListening(speechRecognizerIntent) }
        chatButton.onTouchUp = { speechRecognizer.stopListening() }

    }

    private fun onPress(flag: String) {
        if (flag == "camera") {
            speakOut("Capturing image...")
        }
        Log.d("MainActivity", "clicked")
        val jsonObject = JSONObject()
        jsonObject.put("message", "hello")
        mSocket.emit(flag, jsonObject)
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    inner class SpeechRecognitionListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            Log.d("MainActivity", "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            Log.d("MainActivity", "onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.d("MainActivity", "onRmsChanged")
        }

        override fun onBufferReceived(buffer: ByteArray) {
            Log.d("MainActivity", "onBufferReceived")
        }

        override fun onEndOfSpeech() {
            Log.d("MainActivity", "onEndOfSpeech")
        }

        override fun onError(error: Int) {
            when (error) {
                SpeechRecognizer.ERROR_AUDIO                    -> Log.d("MainActivity", "Audio recording error.")
                SpeechRecognizer.ERROR_CLIENT                   -> Log.d("MainActivity", "Client side error.")
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> Log.d("MainActivity", "Insufficient permissions.")
                SpeechRecognizer.ERROR_NETWORK                  -> Log.d("MainActivity", "Network error.")
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT          -> Log.d("MainActivity", "Network timeout.")
                SpeechRecognizer.ERROR_NO_MATCH                 -> Log.d("MainActivity", "No match.")
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY          -> Log.d("MainActivity", "Recognition service is busy.")
                SpeechRecognizer.ERROR_SERVER                   -> Log.d("MainActivity", "Server error.")
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT           -> Log.d("MainActivity", "No speech input.")
                else -> Log.d("MainActivity", "Unknown error.")
            }
        }

        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null) {
                val spokenText = matches[0]
                Log.d("MainActivity", spokenText)
                val jsonObject = JSONObject()
                jsonObject.put("message", spokenText)
                mSocket.emit("chat", jsonObject)
            }
        }

        override fun onPartialResults(partialResults: Bundle) {
            Log.d("MainActivity", "onPartialResults")
        }

        override fun onEvent(eventType: Int, params: Bundle) {
            Log.d("MainActivity", "onEvent")
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
