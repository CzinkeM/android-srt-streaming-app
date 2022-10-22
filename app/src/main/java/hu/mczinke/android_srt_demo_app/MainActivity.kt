package hu.mczinke.android_srt_demo_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaFormat
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.util.Size
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.github.thibaultbee.streampack.data.AudioConfig
import io.github.thibaultbee.streampack.data.VideoConfig
import io.github.thibaultbee.streampack.ext.srt.streamers.CameraSrtLiveStreamer
import io.github.thibaultbee.streampack.internal.encoders.IEncoderListener
import io.github.thibaultbee.streampack.internal.encoders.VideoMediaCodecEncoder
import io.github.thibaultbee.streampack.views.AutoFitSurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    lateinit var streamer : CameraSrtLiveStreamer
    lateinit var localhostIp : String

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted){
            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
        }
    }

    private val audioConfig = AudioConfig(
        startBitrate = 128000,
        sampleRate = 44100,
        channelConfig = AudioFormat.CHANNEL_IN_MONO
    )

    // TODO: Should tweak the numbers
    private val videoConfig = VideoConfig(
        startBitrate = 2000000, // 2 Mb/s
        resolution = Size(1280, 720),
        fps = 30,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestCameraPermissions()
        requestMicrophonePermissions()
        requestStoragePermissions()

        streamer = CameraSrtLiveStreamer(context = this)
        streamer.configure(audioConfig, videoConfig)


        val preview = findViewById<AutoFitSurfaceView>(R.id.stream_preview)

        val startPreviewButton = findViewById<Button>(R.id.button_preview_start).setOnClickListener {
            streamer.startPreview(preview)
        }

        val endElementListener = findViewById<Button>(R.id.button_preview_end).setOnClickListener {
            streamer.stopPreview()
        }

        val startStreamButton = findViewById<Button>(R.id.button_stream_start).setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                if(!localhostIp.isNullOrBlank()){
                    streamer.startStream("url ir ip/port")// TODO: "url"
                    Log.i(TAG, "onCreate: stream id:${streamer.streamId}")
                }else {
                    Toast.makeText(applicationContext, "please set ip", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val endStreamButton = findViewById<Button>(R.id.button_stream_end).setOnClickListener {
            streamer.stopStream()
        }

        val releaseButton = findViewById<Button>(R.id.button_release).setOnClickListener {
            streamer.release()
        }

        val disconnectButton = findViewById<Button>(R.id.button_disconnect).setOnClickListener {
            streamer.disconnect()
        }


    }

    private fun requestCameraPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this,"Camera permission previously granted",Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA) -> {
                    Toast.makeText(this,"Show camera permission dialog", Toast.LENGTH_SHORT).show()
                }

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun requestMicrophonePermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this,"Microphone permission previously granted",Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(this,"Show microphone permission dialog", Toast.LENGTH_SHORT).show()
            }

            else -> requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun requestStoragePermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this,"Storage permission previously granted",Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                Toast.makeText(this,"Show Storage permission dialog", Toast.LENGTH_SHORT).show()
            }

            else -> requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}