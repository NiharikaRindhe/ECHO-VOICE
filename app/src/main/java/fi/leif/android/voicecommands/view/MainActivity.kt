package fi.leif.android.voicecommands.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.audio.SpeechToText
import fi.leif.android.voicecommands.audio.SpeechToTextFactory
import fi.leif.android.voicecommands.audio.SpeechToTextListener
import fi.leif.android.voicecommands.executors.CommandLauncher
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.android.voicecommands.view.custom.SoundLevelVisualizer
import fi.leif.android.voicecommands.view.custom.TextMatchVisualizer
import fi.leif.voicecommands.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SpeechToTextListener {

    companion object {
        const val EXECUTE_DELAY_MS = 2000L
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var commandLauncher: CommandLauncher

    @Inject
    lateinit var sttFactory: SpeechToTextFactory
    private lateinit var stt: SpeechToText

    private var recMaxRms = 0f
    private var firstLoad = true

    init {
        CoroutineScope(Dispatchers.Main).launch  {
            settingsRepository.getSettings().collect { settings ->
                soundMeter.maxLevel = settings.maxRms
                recMaxRms = settings.recMaxRms
                if(firstLoad) {
                    startListening(settings)
                    firstLoad = false
                }
            }
        }
    }

    private lateinit var textMatcher: TextMatchVisualizer
    private lateinit var soundMeter: SoundLevelVisualizer

    private fun startListening(settings: Settings) {
        stt = sttFactory.create(this)
        stt.start(this, settings.extraLanguage)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        // Visualizers
        textMatcher = findViewById(R.id.text_matcher)
        soundMeter = findViewById(R.id.soundLevel)
        // Settings button
        val setIcon: ImageView = findViewById(R.id.settings)
        setIcon.setOnClickListener {
            stop()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        // Quit if layout clicked
        val layout: RelativeLayout = findViewById(R.id.layout)
        layout.setOnTouchListener { v, event ->
            quit()
            v?.onTouchEvent(event) ?: true
        }
    }

    private fun stop() {
        stt.stop()
        soundMeter.stop()
        textMatcher.stop()
    }

    private fun quit() {
        stop()
        finishAffinity()
    }

    override fun onPartialResult(text: String) {
        if(text.isEmpty()) return
        val commandWord = commandLauncher.textContainsCommand(text)
        textMatcher.highlightMatch(text.lowercase(), commandWord)
    }

    override fun onResult(text: String) {
        if(text.isEmpty()) return
        val commandWord = commandLauncher.textContainsCommand(text)
        textMatcher.highlightMatch(text.lowercase(), commandWord)
        // Execute with delay giving time to show highlight
        lifecycleScope.launch {
            delay(EXECUTE_DELAY_MS)
            stop()
            commandLauncher.executeCommand(this@MainActivity, commandWord, text)
            quit()
        }
    }

    override fun onRmsChanged(rmsDb: Float) {
        // Save maximum received value for reference
        if(recMaxRms < rmsDb) {
            lifecycleScope.launch { settingsRepository.setRecMaxRms(rmsDb) }
        }
        soundMeter.level = rmsDb
    }

    override fun onError() {
        quit()
    }
}
