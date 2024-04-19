package fi.leif.android.voicecommands.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Action.GOOGLE_MAPS
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.Settings
import kotlinx.coroutines.launch

class EditCommandViewModel(application: Application) : AndroidViewModel(application) {

    // Repositories
    // // TODO: Dependency injection
    private val settingsRepository = SettingsRepository(application)

    private var _settings: Settings? = null

    init {
        viewModelScope.launch {
            launch { settingsRepository.getSettings().collect { _settings = it } }
        }
    }

    // Update mode
    private var _mode: EditMode = EditMode.NEW_COMMAND
    private var _updateCommandIndex = 0
    fun setMode(editMode: EditMode, existingCommandIndex: Int?) {
        _mode = editMode
        if(editMode == EditMode.DEFAULT_COMMAND) {
            val command = _settings?.defaultCommand!!
            setSelectedAction(command.action)
        } else if(editMode == EditMode.UPDATE_COMMAND && existingCommandIndex != null) {
            _updateCommandIndex = existingCommandIndex
            val command = _settings?.getCommands(existingCommandIndex)!!
            _commandWords.value?.addAll(command.wordsList)
            setSelectedAction(command.action)
        }
    }

    private var _commandWords = MutableLiveData<ArrayList<String>>(ArrayList())
    val commandWords: LiveData<ArrayList<String>> = _commandWords
    fun addCommandWord(word: String) {
        _commandWords.value?.add(word)
        _commandWords.value = commandWords.value // Trigger observer
    }

    fun removeCommandWord(word: String) {
        _commandWords.value?.remove(word)
        _commandWords.value = commandWords.value // Trigger observer
    }

    private var _selectedAction = MutableLiveData(GOOGLE_MAPS)
    val selectedAction: LiveData<Action> = _selectedAction
    fun setSelectedAction(action: Action) {
        _selectedAction.value = action
    }

    private var _isSaved = MutableLiveData(false)
    val isSaved: LiveData<Boolean> = _isSaved
    fun save(params: Map<String, String>) {
        // Build Command
        val builder = Command.newBuilder()
        builder.action = selectedAction.value
        builder.addAllWords(commandWords.value)
        builder.putAllParameters(params)
        val command = builder.build()

        // Save co-routine
        viewModelScope.launch {
            _isSaved.value = false
            when(_mode) {
                EditMode.DEFAULT_COMMAND -> settingsRepository.updateDefaultCommand(command)
                EditMode.UPDATE_COMMAND -> settingsRepository.updateCommand(_updateCommandIndex, command)
                else -> settingsRepository.addCommand(command)
            }
            _isSaved.value = true
        }
    }

    private var _validationError = MutableLiveData<ValidationError>()
    val validationError: LiveData<ValidationError> = _validationError
    fun isValid(): Boolean {
        if(_mode != EditMode.DEFAULT_COMMAND && commandWords.value?.size == 0) {
            _validationError.value = ValidationError.NO_KEYWORDS
            return false
        }
        return true
    }

    fun isValidKeyword(word: String): Boolean {
        if(word.isEmpty()) {
            _validationError.value = ValidationError.EMPTY_KEYWORD
            return false
        }
        if(commandWords.value?.contains(word) == true) {
            _validationError.value = ValidationError.KEYWORD_EXISTS
            return false
        }
        val exists: Command? = _settings?.commandsList?.find{ it.wordsList.contains(word) }
        exists?.let {
            _validationError.value = ValidationError.KEYWORD_EXISTS
            return false
        }
        return true
    }
}
