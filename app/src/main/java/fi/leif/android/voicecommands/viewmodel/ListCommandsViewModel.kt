package fi.leif.android.voicecommands.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.voicecommands.Command
import kotlinx.coroutines.launch

class ListCommandsViewModel (application: Application) : AndroidViewModel(application) {

    // // TODO: Dependency injection
    private val _settingsRepository = SettingsRepository(application)

    private var _commands = MutableLiveData<List<Command>>()
    val commands: LiveData<List<Command>> = _commands

    var isDeleted = MutableLiveData(false)

    init {
        viewModelScope.launch {
            launch {
                _settingsRepository.getSettings().collect {
                    _commands.value = it.commandsList
                }
            }
        }
    }

    fun deleteCommand(position: Int) {
        viewModelScope.launch {
            isDeleted.value = false
            _settingsRepository.deleteCommand(position)
            isDeleted.value = true
        }
    }

}