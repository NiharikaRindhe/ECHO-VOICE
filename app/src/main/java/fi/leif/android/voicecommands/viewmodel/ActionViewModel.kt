package fi.leif.android.voicecommands.viewmodel

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.leif.android.voicecommands.repositories.App
import fi.leif.android.voicecommands.repositories.AppRepository
import fi.leif.android.voicecommands.repositories.Contact
import fi.leif.android.voicecommands.repositories.ContactsRepository
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActionViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val contactsRepository: ContactsRepository,
    private val appRepository: AppRepository
) : AndroidViewModel(application) {

    private var settings: Settings? = null

    suspend fun fetchSettings() {
        settings = settingsRepository.getSettings().first()
    }

    private var _command = MutableLiveData<Command>()
    val command: LiveData<Command> = _command
    fun setCommandByIndex(commandIndex: Int) {
        _command.value = settings?.getCommands(commandIndex)
    }
    fun setDefaultCommand() {
        _command.value = settings?.defaultCommand
    }

    private var _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    private var _noRtcContacts = MutableLiveData(false)
    val noRtcContacts = _noRtcContacts
    private suspend fun handleNoRtcContacts() {
        fetchPhoneContacts()
        _noRtcContacts.value = true
    }

    private var _whatsAppContacts = MutableLiveData<List<Contact>>()
    fun fetchWhatsAppContacts() {
        if(_whatsAppContacts.value == null) {
            viewModelScope.launch {
                val contacts = contactsRepository.getWhatsAppVoipContacts()
                if(contacts.isNotEmpty()) {
                    _noRtcContacts.value = false
                    _whatsAppContacts.value = contactsRepository.getWhatsAppVoipContacts()
                    _contacts.value = _whatsAppContacts.value
                } else {
                    handleNoRtcContacts()
                }
            }
        } else {
            _contacts.value = _whatsAppContacts.value
        }
    }

    private var _telegramContacts = MutableLiveData<List<Contact>>()
    fun fetchTelegramContacts() {
        if(_telegramContacts.value == null) {
            viewModelScope.launch {
                val contacts = contactsRepository.getTelegramCallContacts()
                if(contacts.isNotEmpty()) {
                    _noRtcContacts.value = false
                    _telegramContacts.value = contactsRepository.getTelegramCallContacts()
                    _contacts.value = _telegramContacts.value
                } else {
                    handleNoRtcContacts()
                }
            }
        } else {
            _contacts.value = _telegramContacts.value
        }
    }

    private var _phoneContacts = MutableLiveData<List<Contact>>()
    suspend fun fetchPhoneContacts() {
        if(_phoneContacts.value == null) {
                _phoneContacts.value = contactsRepository.getPhoneContacts()
                _contacts.value = _phoneContacts.value
        } else {
            _contacts.value = _phoneContacts.value
        }
    }

    private var _rtcTypeNames = MutableLiveData<List<String>>()
    val rtcTypeNames: LiveData<List<String>> = _rtcTypeNames
    fun setRtcTypeNames(rtcTypeNames: List<String>) {
        _rtcTypeNames.value = rtcTypeNames
    }

    private var _selectedRtcTypeName = MutableLiveData("")
    val selectedRtcTypeName: LiveData<String> = _selectedRtcTypeName
    fun setSelectedRtcTypeName(name: String?) {
        _selectedRtcTypeName.value = name
    }

    fun setSelectedRtcTypeNameByPosition(position: Int) {
        _selectedRtcTypeName.value = _rtcTypeNames.value?.get(position)
    }

    var selectedContact = ObservableField(Contact("","", ""))
    fun setSelectedContactByPosition(position: Int) {
        selectedContact.set(_contacts.value?.get(position))
    }
    fun setSelectedContactById(value: String) {
        selectedContact.set(_contacts.value?.firstOrNull { it.id == value })
    }

    private var _apps = MutableLiveData<List<App>>()
    val apps: LiveData<List<App>> = _apps
    fun fetchApps() {
        if(_apps.value == null) {
            viewModelScope.launch {
                _apps.value = appRepository.getApplications()
                setSelectedAppByPosition(0)
            }
        }
    }

    private var _selectedApp = MutableLiveData<App>()
    val selectedApp:LiveData<App> = _selectedApp
    fun setSelectedAppByPosition(position: Int) {
        _selectedApp.value = _apps.value?.get(position)
    }
    fun setSelectedAppByValue(value: String) {
        _selectedApp.value = _apps.value?.firstOrNull { it.pkg == value }
    }

    private var _searchValue = MutableLiveData<String>()
    val searchValue:LiveData<String> = _searchValue
    fun setSearchValue(value: String?) {
        value?.let { _searchValue.value = it }
    }

    private var _destination = MutableLiveData<String>()
    val destination:LiveData<String> = _destination
    fun setDestination(destination: String?) {
        destination?.let { _destination.value = it }
    }

}