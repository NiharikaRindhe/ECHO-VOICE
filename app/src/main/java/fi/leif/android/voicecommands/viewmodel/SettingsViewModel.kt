package fi.leif.android.voicecommands.viewmodel

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.leif.android.voicecommands.repositories.LanguageRepository
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.voicecommands.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val languageRepository: LanguageRepository,
) : AndroidViewModel(application) {

    suspend fun fetchRepositories() {
        settings = settingsRepository.getSettings().first()
        _recMaxRmsDb.value = settings.recMaxRms.toString()
        maxRmsDb.set(settings.maxRms.toString())
        initLanguages(settings.extraLanguage)
    }

    private var settings = Settings.getDefaultInstance()

    private var _supportedLanguages: MutableLiveData<List<String>> = MutableLiveData()
    val supportedLanguages: LiveData<List<String>> = _supportedLanguages

    val selectedExtraLanguage = ObservableField<String>()

    fun setSelectedExtraLanguage(position: Int) {
        val langCode = languageRepository.getLanguageCodeByIndex(position)
        viewModelScope.launch {
            settingsRepository.setExtraLanguage(langCode)
            Log.d("SettingsViewModel", "Language updated to: $langCode")
        }
    }

    private suspend fun initLanguages(extraLanguage: String) {
        languageRepository.getSupportedLanguages().asFlow().collect { langs ->
            if (langs.isNullOrEmpty()) {
                Log.e("SettingsViewModel", "Language list is empty, applying fallback.")
                _supportedLanguages.value = listOf("English (United States)", "Hindi (India)")
                selectedExtraLanguage.set("English (United States)") // Default selection
                return@collect
            }

            _supportedLanguages.value = langs
            val index = languageRepository.getIndexByLanguageCode(extraLanguage)

            if (index in langs.indices) {
                selectedExtraLanguage.set(langs[index])
            } else {
                Log.e("SettingsViewModel", "Invalid language index: $index. Using default.")
                selectedExtraLanguage.set(langs[0]) // Default to the first language
            }
        }
    }

    val systemLanguage: LiveData<String> = MutableLiveData(Locale.getDefault().displayLanguage)

    private var _validationError = MutableLiveData<ValidationError>()
    val validationError: LiveData<ValidationError> = _validationError

    val maxRmsDb = ObservableField<String>()
    fun setMaxRmsDb(text: ObservableField<String>) {
        val number = text.get()?.toFloatOrNull()
        if (number != null && number in 1f..100f) {
            viewModelScope.launch {
                settingsRepository.setMaxRms(number)
                Log.d("SettingsViewModel", "Max RMS updated to: $number")
            }
        } else {
            _validationError.value = ValidationError.INVALID_RMS_DB
        }
    }

    private var _recMaxRmsDb: MutableLiveData<String> = MutableLiveData()
    val recMaxRmsDb: LiveData<String> = _recMaxRmsDb
}
