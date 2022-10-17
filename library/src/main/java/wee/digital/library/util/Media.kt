package wee.digital.library.util

import android.media.AudioAttributes
import android.media.SoundPool
import wee.digital.library.app


object Media {

    var isSilent: Boolean = false

    private val soundMap = mutableMapOf<Int, Int?>()

    private val soundPool: SoundPool by lazy {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        return@lazy SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attrs)
            .build()
    }

    fun play(raw: Int) {
        if (isSilent) return
        val soundId = soundMap[raw]
        if (soundId == null) {
            soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
                if (status == 0) {
                    soundPool.play(sampleId, 1f, 1f, 1, 0, 1.0f)
                }
            }
            soundMap[raw] = soundPool.load(app, raw, 1)
        } else {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1.0f)
        }
    }

}

