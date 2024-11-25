package fi.leif.android.voicecommands.executors

import android.content.ActivityNotFoundException
import android.content.Context
import android.util.Log
import fi.leif.android.voicecommands.mappers.ActionMapper
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class CommandLauncher @Inject constructor(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    private val wordsMap = linkedMapOf<String, Command>()
    private var defaultCommand: Command = Command.getDefaultInstance()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            settingsRepository.getSettings().collect {
                buildWordsActionsMap(it.commandsList)
                defaultCommand = it.defaultCommand
            }
        }
    }

    private fun buildWordsActionsMap(commands: List<Command>) {
        val unsortedMap = HashMap<String, Command>()
        for (cmd in commands) for (word in cmd.wordsList) {
            unsortedMap[word] = cmd
        }
        // Create a Map of all command words sorted by longest word first
        unsortedMap.entries.sortedByDescending { it.key.length }.forEach { (word, command) ->
            wordsMap[word] = command
        }
    }

    fun textContainsCommand(text: String): String? {
        val lowerText = text.lowercase()
        if (lowerText.startsWith("open ")) {
            return "open"
        }

        wordsMap.entries.forEach { (word, _) ->
            if (lowerText.contains(word.lowercase())) {
                return word
            }
        }
        return null
    }

    suspend fun executeCommand(context: Context, cmdWord: String?, text: String) {
        val command = wordsMap[cmdWord]
        try {
            if (command != null) {
                // Execute the predefined command
                ActionMapper.getActionExecutor(command.action)
                    .execute(context, text, command)
            } else {
                // Fall back to dynamic app opening if no predefined command matches
                val openAppExecutor = OpenAppExecutor()
                openAppExecutor.execute(context, text, defaultCommand)
            }
        } catch (e: ActivityNotFoundException) {
            Log.e("CommandLauncher", "Activity not found for the command: $text", e)
        }
    }
}


