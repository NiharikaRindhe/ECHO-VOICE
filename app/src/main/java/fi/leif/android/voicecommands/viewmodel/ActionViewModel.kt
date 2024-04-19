package fi.leif.android.voicecommands.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import fi.leif.android.voicecommands.repositories.App
import fi.leif.android.voicecommands.repositories.AppRepository
import fi.leif.android.voicecommands.repositories.Contact
import fi.leif.android.voicecommands.repositories.ContactsRepository
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.RtcType
import fi.leif.voicecommands.Settings
import kotlinx.coroutines.launch

class ActionViewModel(application: Application) : AndroidViewModel(application) {

    // TODO: Dependency injection
    private val contactsRepository = ContactsRepository(application)
    private val settingsRepository = SettingsRepository(application)
    private val appRepository = AppRepository(application)

    private var _settings: Settings? = null

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { _settings = it }
        }
    }

    private var _command = MutableLiveData<Command>()
    val command: LiveData<Command> = _command
    fun setCommand(commandIndex: Int) {
        _command.value = _settings?.getCommands(commandIndex)
    }
    fun setDefaultCommand() {
        _command.value = _settings?.defaultCommand
    }

    private var _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    private var _whatsAppContacts = MutableLiveData<List<Contact>>()
    fun fetchWhatsAppContacts() {
        // TODO: RTC Type requires difference contact types?
        if(_whatsAppContacts.value == null) {
            viewModelScope.launch {
                _whatsAppContacts.value = contactsRepository.getWhatsAppVoipContacts()
                _contacts.value = _whatsAppContacts.value
            }
        } else {
            _contacts.value = _whatsAppContacts.value
        }
    }

    private var _telegramContacts = MutableLiveData<List<Contact>>()
    fun fetchTelegramContacts() {
        if(_telegramContacts.value == null) {
            viewModelScope.launch {
                _telegramContacts.value = contactsRepository.getTelegramCallContacts()
                _contacts.value = _telegramContacts.value
            }
        } else {
            _contacts.value = _telegramContacts.value
        }
    }

    private var _phoneContacts = MutableLiveData<List<Contact>>()
    fun fetchPhoneContacts() {
        if(_phoneContacts.value == null) {
            viewModelScope.launch {
                _phoneContacts.value = contactsRepository.getPhoneContacts()
                _contacts.value = _phoneContacts.value
            }
        } else {
            _contacts.value = _phoneContacts.value
        }
    }

    private var _selectedRtcType = MutableLiveData(RtcType.AUDIO_CALL)
    val selectedRtcType: LiveData<RtcType> = _selectedRtcType
    fun setSelectedRtcType(rtcType: RtcType?) {
        _selectedRtcType.value = rtcType
    }

    private var _selectedContact = MutableLiveData(Contact("","", ""))
    val selectedContact: LiveData<Contact> = _selectedContact
    fun setSelectedContactByPosition(position: Int) {
        _selectedContact.value = _contacts.value?.get(position)
    }
    fun setSelectedContactByValue(value: String) {
        _selectedContact.value = _contacts.value?.firstOrNull() { it.id == value }
    }
    fun setSelectedContact(contact: Contact?) {
        _selectedContact.value = contact
    }

    private var _apps = MutableLiveData<List<App>>()
    val apps: LiveData<List<App>> = _apps
    fun fetchApps() {
        if(_apps.value == null) {
            viewModelScope.launch {
                _apps.value = appRepository.getApplications()
            }
        }
    }

    private var _selectedApp = MutableLiveData<App>()
    val selectedApp:LiveData<App> = _selectedApp
    fun setSelectedAppByPosition(position: Int) {
        _selectedApp.value = _apps.value?.get(position)
    }
    fun setSelectedAppByValue(value: String) {
        _selectedApp.value = _apps.value?.firstOrNull() { it.pkg == value }
    }

}