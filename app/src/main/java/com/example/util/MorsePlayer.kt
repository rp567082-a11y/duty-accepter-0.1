package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MorsePlayer {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    suspend fun playMorseCode(morseText: String, onProgress: (Int) -> Unit = {}) = withContext(Dispatchers.IO) {
        val dotDuration = 120L // ms for a dot
        val dashDuration = dotDuration * 3
        val intraCharSpace = dotDuration
        val interCharSpace = dotDuration * 3
        val wordSpace = dotDuration * 7

        var idx = 0
        val totalChars = morseText.length

        for (char in morseText) {
            onProgress(idx)
            when (char) {
                '.' -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, dotDuration.toInt())
                    delay(dotDuration + intraCharSpace)
                }
                '-' -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, dashDuration.toInt())
                    delay(dashDuration + intraCharSpace)
                }
                ' ' -> {
                    delay(interCharSpace)
                }
                '/' -> {
                    delay(wordSpace)
                }
                else -> {
                    delay(dotDuration)
                }
            }
            idx++
        }
        onProgress(totalChars)
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
