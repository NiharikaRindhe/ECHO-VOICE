package fi.leif.android.voicecommands.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import fi.leif.android.voicecommands.repositories.LanguageRepository
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.voicecommands.Settings
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // TODO: Dependency inject
    private val _settingsRepository = SettingsRepository(application)
    private val _languageRepository = LanguageRepository(application)

    private var _settings = Settings.getDefaultInstance()

    private var _supportedLanguages: MutableLiveData<List<String>> = MutableLiveData()
    val supportedLanguages: LiveData<List<String>> = _supportedLanguages

    val systemLanguage: LiveData<String> = MutableLiveData(Locale.getDefault().displayLanguage)

    private var _validationError = MutableLiveData<ValidationError>()
    val validationError: LiveData<ValidationError> = _validationError

    fun getMaxRmsDb(): String {
        return _settings.maxRms.toString()
    }
    fun setMaxRmsDb(text: String) {
            val number = text.toFloatOrNull()
            if (number != null && number in 1f..100f) {
                viewModelScope.launch {
                    _settingsRepository.setMaxRms(number)
                }
            } else {
                _validationError.value = ValidationError.INVALID_RMS_DB
            }
    }
    private var _recMaxRmsDb: MutableLiveData<String> = MutableLiveData()
    val recMaxRmsDb: LiveData<String> = _recMaxRmsDb

    init {
        viewModelScope.launch {
            _settingsRepository.getSettings().collect { settings ->
                _settings = settings
                _recMaxRmsDb.value = settings.recMaxRms.toString()
                _languageRepository.getSupportedLanguages().asFlow().collect { langs ->
                    _supportedLanguages.value = langs
                }
            }
        }
    }

    fun getSettingsExtraLanguageIndex(): Int {
        return _languageRepository.getIndexByLanguageCode(_settings.extraLanguage)
    }

    fun setLanguageCodeByPosition(selecedIndex: Int) {
        val langCode = _languageRepository.getLanguageCodeByIndex(selecedIndex)
        // Save changes
        viewModelScope.launch {
            _settingsRepository.setExtraLanguage(langCode)
        }
    }
}