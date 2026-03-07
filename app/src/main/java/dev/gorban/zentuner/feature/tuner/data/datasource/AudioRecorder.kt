package dev.gorban.zentuner.feature.tuner.data.datasource

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class AudioRecorder {

    val sampleRate = 44100
    private val bufferSize = 4096

    private var audioRecord: AudioRecord? = null

    fun start() {
        if (audioRecord != null) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        val actualBufferSize = maxOf(minBufferSize, bufferSize * 4)

        @SuppressLint("MissingPermission")
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
            actualBufferSize
        )

        audioRecord?.startRecording()
    }

    fun stop() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    fun audioFlow(): Flow<FloatArray> = callbackFlow {
        val buffer = FloatArray(bufferSize)
        while (true) {
            val record = audioRecord ?: break
            val read = record.read(buffer, 0, bufferSize, AudioRecord.READ_BLOCKING)
            if (read > 0) {
                trySend(buffer.copyOf(read))
            } else {
                break
            }
        }
        awaitClose()
    }.flowOn(Dispatchers.IO)
}
