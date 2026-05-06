package org.unastaziabo.aicognito.voice

import java.io.File
import javax.sound.sampled.*

object VoiceRecorder {

    fun recordAudio(file: File, seconds: Int = 8) {

        require(seconds > 0) { "Recording duration must be greater than 0" }

        val format = AudioFormat(
            SAMPLE_RATE,
            SAMPLE_SIZE,
            CHANNELS,
            SIGNED,
            LITTLE_ENDIAN
        )

        val info = DataLine.Info(TargetDataLine::class.java, format)

        val line = try {
            AudioSystem.getLine(info) as TargetDataLine
        } catch (e: Exception) {
            throw RuntimeException("Audio input device not available", e)
        }

        line.open(format)
        line.start()

        val audioStream = AudioInputStream(line)

        val recordingThread = Thread {
            try {
                AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, file)
            } catch (e: Exception) {
                throw RuntimeException("Failed to write audio file", e)
            }
        }

        recordingThread.start()

        try {
            Thread.sleep(seconds * 1000L)
        } finally {
            // Ensure recording stops even if interrupted
            line.stop()
            line.close()
        }

        // Wait for file write to complete
        recordingThread.join()
    }

    private const val SAMPLE_RATE = 16000f
    private const val SAMPLE_SIZE = 16
    private const val CHANNELS = 1
    private const val SIGNED = true
    private const val LITTLE_ENDIAN = false
}