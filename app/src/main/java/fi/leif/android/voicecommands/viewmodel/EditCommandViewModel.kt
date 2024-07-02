package fi.leif.android.voicecommands.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.leif.android.voicecommands.mappers.ActionMapper
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Action.GOOGLE_MAPS
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCommandViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository,
) : AndroidViewModel(application) {

    private var settings: Settings? = null

    suspend fun fetchSettings() {
        settings = settingsRepository.getSettings().first()
    }

    // Update mode
    private var _mode: EditMode = EditMode.NEW_COMMAND
    private var _updateCommandIndex = 0
    fun setMode(editMode: EditMode, existingCommandIndex: Int?) {
        _mode = editMode
        if(editMode == EditMode.DEFAULT_COMMAND) {
            val command = settings?.defaultCommand
            command?.action?.let { setSelectedAction(it) }
        } else if(editMode == EditMode.UPDATE_COMMAND && existingCommandIndex != null) {
            _updateCommandIndex = existingCommandIndex
            val command = settings?.getCommands(existingCommandIndex)
            command?.wordsList?.let { _commandWords.value?.addAll(it) }
            command?.action?.let { setSelectedAction(it) }
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

    private var _actions = MutableLiveData<List<String>>()
    val actions: LiveData<List<String>> = _actions
    fun setActions(actions: List<String>){
        _actions.value = actions
    }
    private var _selectedAction = MutableLiveData(GOOGLE_MAPS)
    val selectedAction: LiveData<Action> = _selectedAction
    fun setSelectedAction(action: Action) {
        _selectedAction.value = action
    }
    fun setSelectedActionByPosition(position: Int) {
        // Default command has an additional action
        val pos = if(_mode == EditMode.DEFAULT_COMMAND) position else position+1
        val action = ActionMapper.getActionAt(pos)
        setSelectedAction(action)
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
        val exists: Command? = settings?.commandsList?.find{ it.wordsList.contains(word) }
        exists?.let {
            _validationError.value = ValidationError.KEYWORD_EXISTS
            return false
        }
        return true
    }
}
