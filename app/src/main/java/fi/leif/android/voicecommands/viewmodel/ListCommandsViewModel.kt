package fi.leif.android.voicecommands.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.voicecommands.Command
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListCommandsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository,
) : AndroidViewModel(application) {

    suspend fun fetchCommands() {
        _commands.value = settingsRepository.getSettings().first().commandsList
    }

    private var _commands = MutableLiveData<List<Command>>()
    val commands: LiveData<List<Command>> = _commands

    var isDeleted = MutableLiveData(false)

    fun deleteCommand(position: Int) {
        viewModelScope.launch {
            isDeleted.value = false
            settingsRepository.deleteCommand(position)
            isDeleted.value = true
        }
    }
}