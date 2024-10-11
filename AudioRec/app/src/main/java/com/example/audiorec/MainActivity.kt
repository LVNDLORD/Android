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
                        isPlaying = true // Set to true before starting playback
                        coroutineScope.launch {
                            // Reopen FileInputStream each time to allow for multiple playback
                            playAudio(FileInputStream(recordingFilePath))
                            isPlaying = false // Reset playing state after playback is complete
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
            44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
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
                    .setSampleRate(44100)  // Make sure this matches the recording sample rate
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO) // Use MONO if recorded in MONO
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
