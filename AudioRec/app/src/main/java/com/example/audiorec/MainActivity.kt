package com.example.audiorec
import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {

    private var audioRecorder: AudioRecord? = null
    private var recordingFilePath: String = ""
    private var bufferSize: Int = 0
    private var isPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bufferSize = AudioRecord.getMinBufferSize(
            44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )

        // Register permission request launcher
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            isPermissionGranted = isGranted
            if (!isGranted) {
                Log.e("Permission", "Audio recording permission denied")
            }
        }

        // Check for permissions initially
        checkAndRequestPermissions(requestPermissionLauncher)

        setContent {
            var isRecording by remember { mutableStateOf(false) }
            var isPlaying by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            AudioRecorderApp(
                isRecording = isRecording,
                onStartStopRecording = {
                    if (isRecording) {
                        stopRecording()
                        isRecording = false
                    } else {
                        if (isPermissionGranted) {
                            startRecording()
                            isRecording = true
                        } else {
                            Log.e("Permission", "Permission not granted to record audio")
                        }
                    }
                },
                onPlayAudio = {
                    if (!isPlaying) {
                        coroutineScope.launch {
                            playAudio(FileInputStream(recordingFilePath))
                            isPlaying = true
                        }
                    }
                }
            )
        }
    }

    private fun checkAndRequestPermissions(requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            isPermissionGranted = true
        }
    }

    private fun startRecording() {
        try {
            recordingFilePath = "${externalCacheDir?.absolutePath}/audiorecordtest.pcm"
            audioRecorder = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()

            audioRecorder?.startRecording()

            val data = ByteArray(bufferSize)
            val outputStream = FileOutputStream(recordingFilePath)

            Thread {
                while (audioRecorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val read = audioRecorder?.read(data, 0, bufferSize)
                    if (read != null && read > 0) {
                        outputStream.write(data)
                    }
                }
                outputStream.close()
            }.start()
        } catch (e: SecurityException) {
            Log.e("SecurityException", "Permission denied: ${e.message}")
        }
    }

    private fun stopRecording() {
        audioRecorder?.stop()
        audioRecorder?.release()
        audioRecorder = null
    }

    private suspend fun playAudio(inputStream: FileInputStream) = withContext(Dispatchers.IO) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT
        )

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .build()

        audioTrack.play()

        val buffer = ByteArray(minBufferSize)
        var bytesRead: Int

        try {
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                audioTrack.write(buffer, 0, bytesRead)
            }
        } catch (e: IOException) {
            Log.e("AudioTrack", "Error reading audio data: $e")
        } finally {
            inputStream.close()
            audioTrack.stop()
            audioTrack.release()
        }
    }
}

@Composable
fun AudioRecorderApp(
    isRecording: Boolean,
    onStartStopRecording: () -> Unit,
    onPlayAudio: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { onStartStopRecording() }) {
            Text(text = if (isRecording) "Stop Recording" else "Start Recording")
        }

        if (isRecording) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Recording...", modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onPlayAudio() }) {
            Text(text = "Play Audio")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AudioRecorderApp(
        isRecording = false,
        onStartStopRecording = {},
        onPlayAudio = {}
    )
}




//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.media.AudioAttributes
//import android.media.AudioFormat
//import android.media.AudioManager
//import android.media.AudioRecord
//import android.media.AudioTrack
//import android.media.MediaRecorder
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.Button
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.lifecycleScope
//import kotlinx.coroutines.*
//
//class MainActivity : AppCompatActivity() {
//
//    private val sampleRate = 44100
//    private val bufferSize = AudioRecord.getMinBufferSize(
//        sampleRate,
//        AudioFormat.CHANNEL_IN_MONO,
//        AudioFormat.ENCODING_PCM_16BIT
//    )
//    private val audioBuffer = ShortArray(bufferSize)
//    private val recordedData = mutableListOf<Short>() // Store all recorded audio
//
//    private var audioRecord: AudioRecord? = null
//    private lateinit var audioTrack: AudioTrack
//
//    private var isRecording = false
//    private var isPlaying = false
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            AudioRecorderApp()
//        }
//
//        checkPermissions()
//    }
//
//    @Composable
//    fun AudioRecorderApp() {
//        var isRecording by remember { mutableStateOf(false) }
//        var isPlaying by remember { mutableStateOf(false) }
//
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Button(onClick = {
//                if (!isRecording) {
//                    startRecording()
//                    isRecording = true
//                } else {
//                    stopRecording()
//                    isRecording = false
//                }
//            }) {
//                Text(text = if (isRecording) "Stop Recording" else "Start Recording")
//            }
//
//            if (isRecording) {
//                Spacer(modifier = Modifier.height(16.dp))
//                Text(text = "Recording...", modifier = Modifier.align(Alignment.CenterHorizontally))
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(onClick = {
//                if (!isPlaying) {
//                    startPlayback()
//                    isPlaying = true
//                } else {
//                    isPlaying = false
//                }
//            }) {
//                Text(text = "Play Recording")
//            }
//        }
//    }
//
//    private fun startRecording() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
//            isRecording = true
//            recordedData.clear()
//            lifecycleScope.launch(Dispatchers.IO) {
//                initializeAudioRecord()
//                audioRecord?.startRecording()
//                var totalReadSize = 0 // Track total size of data recorded
//                while (isRecording) {
//                    val readSize = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
//                    totalReadSize += readSize
//                    Log.d("Recording", "Recording audio: $readSize bytes read")
//                }
//                audioRecord?.stop()
//            }
//        } else {
//            Log.d("Permission", "Audio recording permission not granted")
//        }
//    }
//
//    private fun stopRecording() {
//        isRecording = false
//    }
//
//    private fun startPlayback() {
//        if (audioBuffer.isNotEmpty()) {
//            isPlaying = true
//            val bufferSizeInBytes = audioBuffer.size * 2 // Short is 2 bytes
//
//            // Build a new AudioTrack instance using the builder
//            audioTrack = AudioTrack.Builder()
//                .setAudioAttributes(
//                    AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_MEDIA) // For general media playback
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .build()
//                )
//                .setAudioFormat(
//                    AudioFormat.Builder()
//                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT) // same as recording
//                        .setSampleRate(sampleRate) // same sample rate as recording
//                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO) // mono output
//                        .build()
//                )
//                .setBufferSizeInBytes(bufferSizeInBytes) // size of the buffer in bytes
//                .setTransferMode(AudioTrack.MODE_STREAM) // use streaming mode
//                .build()
//
//            lifecycleScope.launch(Dispatchers.IO) {
//                // Start playback
//                audioTrack.play()
//
//                // Write the short buffer to the AudioTrack
//                val readSize = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0 // Only write the actual size read
//                audioTrack.write(audioBuffer, 0, readSize)
//
//                // Let the track play out
//                audioTrack.stop()
//                audioTrack.release()
//
//                isPlaying = false
//            }
//        } else {
//            Log.d("Playback", "Audio buffer is empty, nothing to play")
//        }
//    }
//
//
//
//
//    private fun initializeAudioRecord() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.RECORD_AUDIO
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Request permission if not granted
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.RECORD_AUDIO),
//                200 // Request code for identifying this request
//            )
//            return
//        }
//
//        // Initialize AudioRecord if permission is granted
//        audioRecord = AudioRecord(
//            MediaRecorder.AudioSource.MIC,
//            sampleRate,
//            AudioFormat.CHANNEL_IN_MONO,
//            AudioFormat.ENCODING_PCM_16BIT,
//            bufferSize
//        )
//
//        // Initialize AudioTrack for playback
//        audioTrack = AudioTrack.Builder()
//            .setAudioFormat(
//                AudioFormat.Builder()
//                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//                    .setSampleRate(sampleRate)
//                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
//                    .build()
//            )
//            .setBufferSizeInBytes(bufferSize)
//            .build()
//    }
//
//    private fun checkPermissions() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            // Request permission
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.RECORD_AUDIO),
//                200 // Request code for permission
//            )
//        }
//    }
//
//    // Handle the permission result
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 200) {
//            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                // Permission granted, proceed with initializing AudioRecord
//                initializeAudioRecord()
//            } else {
//                Log.d("Permission", "Audio recording permission was denied")
//            }
//        }
//    }
//}

//
//
////package com.example.audiorec
////
////import android.Manifest
////import android.content.pm.PackageManager
////import android.media.AudioFormat
////import android.media.AudioRecord
////import android.media.AudioTrack
////import android.media.MediaRecorder
////import android.os.Bundle
////import android.util.Log
////import androidx.activity.compose.setContent
////import androidx.activity.result.contract.ActivityResultContracts
////import androidx.appcompat.app.AppCompatActivity
////import androidx.compose.foundation.layout.*
////import androidx.compose.material3.Button
////import androidx.compose.material3.Text
////import androidx.compose.runtime.*
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.unit.dp
////import androidx.core.content.ContextCompat
////import androidx.lifecycle.lifecycleScope
////import kotlinx.coroutines.*
////
////class MainActivity : AppCompatActivity() {
////
////    private val sampleRate = 44100
////    private val bufferSize = AudioRecord.getMinBufferSize(
////        sampleRate,
////        AudioFormat.CHANNEL_IN_MONO,
////        AudioFormat.ENCODING_PCM_16BIT
////    )
////    private val audioBuffer = ShortArray(bufferSize)
////
////    private var audioRecord: AudioRecord? = null
////    private lateinit var audioTrack: AudioTrack
////
////    private var isRecording = false
////    private var isPlaying = false
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContent {
////            AudioRecorderApp()
////        }
////
////        checkPermissions()
////
////        audioTrack = AudioTrack.Builder()
////            .setAudioFormat(
////                AudioFormat.Builder()
////                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
////                    .setSampleRate(sampleRate)
////                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
////                    .build()
////            )
////            .setBufferSizeInBytes(bufferSize)
////            .build()
////    }
////
////    @Composable
////    fun AudioRecorderApp() {
////        var isRecording by remember { mutableStateOf(false) }
////        var isPlaying by remember { mutableStateOf(false) }
////
////        Column(
////            modifier = Modifier.fillMaxSize(),
////            horizontalAlignment = Alignment.CenterHorizontally,
////            verticalArrangement = Arrangement.Center
////        ) {
////            Button(onClick = {
////                if (!isRecording) {
////                    startRecording()
////                    isRecording = true
////                } else {
////                    stopRecording()
////                    isRecording = false
////                }
////            }) {
////                Text(text = if (isRecording) "Stop Recording" else "Start Recording")
////            }
////
////            Spacer(modifier = Modifier.height(16.dp))
////
////            Button(onClick = {
////                if (!isPlaying) {
////                    startPlayback()
////                    isPlaying = true
////                } else {
////                    isPlaying = false
////                }
////            }) {
////                Text(text = "Play Recording")
////            }
////        }
////    }
////
////    private fun startRecording() {
////        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
////            isRecording = true
////            lifecycleScope.launch(Dispatchers.IO) {
////                audioRecord?.startRecording()
////                while (isRecording) {
////                    val readSize = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: 0
////                    Log.d("Recording", "Recording audio: $readSize bytes read")
////                }
////                audioRecord?.stop()
////            }
////        } else {
////            Log.d("Permission", "Audio recording permission not granted")
////        }
////    }
////
////    private fun stopRecording() {
////        isRecording = false
////    }
////
////    private fun startPlayback() {
////        isPlaying = true
////        lifecycleScope.launch(Dispatchers.IO) {
////            audioTrack.play()
////            audioTrack.write(audioBuffer, 0, audioBuffer.size)
////            audioTrack.stop()
////            isPlaying = false
////        }
////    }
////
////    private fun checkPermissions() {
////        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
////            val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
////                if (!isGranted) {
////                    Log.d("Permission", "Audio recording permission not granted")
////                }
////            }
////            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
////        }
////    }
////}
