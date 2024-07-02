package fi.leif.android.voicecommands.audio

interface SpeechToTextFactory {
    fun create(listener: SpeechToTextListener): SpeechToText
}

class DefaultSpeechToTextFactory : SpeechToTextFactory {
    override fun create(listener: SpeechToTextListener): SpeechToText {
        return SpeechToText(listener)
    }
}