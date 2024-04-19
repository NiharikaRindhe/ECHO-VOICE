package fi.leif.android.voicecommands.executors

import android.content.Context
import android.content.Intent
import android.net.Uri
import fi.leif.android.voicecommands.repositories.ContactsRepository
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys
import fi.leif.voicecommands.RtcType.*

class WhatsAppExecutor: Executor(
        Action.WHATSAPP,
        "com.whatsapp"
    ) {

    override fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent? {
        // Contact from settings, or match to spoken name
        val rtcType = getParameter(configCommand, ParameterKeys.RTC_TYPE)
            ?: AUDIO_CALL.toString() // Default to audio call
        return when(valueOf(rtcType)) {
            AUDIO_CALL -> {
                val settingsContactId = getParameter(configCommand, ParameterKeys.CONTACT_ID)
                getAudioCallIntent(context, settingsContactId,cleanText)
            }
            MESSAGE-> {
                val settingsContactPhone = getParameter(configCommand, ParameterKeys.CONTACT_PHONE)
                getMessageIntent(settingsContactPhone,cleanText)
            }
            else -> null
        }
    }

    private fun getAudioCallIntent(context: Context,
                                   settingsContactId: String?,
                                   cleanText: String): Intent? {
        val intent = Intent(Intent.ACTION_VIEW)
        // Contact configured in settings
        if(settingsContactId != null) {
            intent.setDataAndType(
                Uri.parse("content://com.android.contacts/data/$settingsContactId"),
                ContactsRepository.MIMETYPE_WHATSAPP_VOIP
            )
        }
        // Contact from spoken name
        else {
            val contactsRepo = ContactsRepository(context)
            val contacts = contactsRepo.getWhatsAppVoipContacts()
            val contactMatch = contactsRepo
                .findClosestMatchingName(contacts, cleanText) ?: return null
            intent.setDataAndType(Uri.parse(
                "content://com.android.contacts/data/${contactMatch.id}"),
                ContactsRepository.MIMETYPE_WHATSAPP_VOIP
            )
        }
        return intent
    }

    private fun getMessageIntent(settingsContactPhone: String?,
                                 cleanText: String): Intent? {
        // We allow only messages sent if a recipient is set in the settings
        if(settingsContactPhone == null) {
            return null
        }
        val uri = Uri.parse("https://api.whatsapp.com/send?"+
                "phone=$settingsContactPhone&text=${Uri.encode(cleanText)}")
        return Intent(Intent.ACTION_VIEW, uri)
    }

}