package fi.leif.android.voicecommands.executors

import android.content.Context
import android.content.Intent
import android.net.Uri
import fi.leif.android.voicecommands.repositories.ContactsRepository
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys
import fi.leif.voicecommands.RtcType

class TelegramExecutor : Executor(
    Action.TELEGRAM,
    "org.telegram.messenger"
) {
    override fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent? {
        // Contact from settings, or match to spoken name
        val rtcType = getParameter(configCommand, ParameterKeys.RTC_TYPE)
            ?: RtcType.AUDIO_CALL.toString() // Default to audio call
        return when(RtcType.valueOf(rtcType)) {
            RtcType.AUDIO_CALL -> {
                val settingsContactId = getParameter(configCommand, ParameterKeys.CONTACT_ID)
                getAudioCallIntent(context, settingsContactId,cleanText)
            }
            RtcType.MESSAGE -> {
                val settingsContactName = getParameter(configCommand, ParameterKeys.CONTACT_NAME)
                getMessageIntent(context, settingsContactName, cleanText)
            }
            else -> null
        }
    }

    private fun getAudioCallIntent(
        context: Context,
        settingsContactId: String?,
        cleanText: String
    ): Intent? {
        val intent = Intent(Intent.ACTION_VIEW)
        // Contact configured in settings
        if(settingsContactId != null) {
            intent.setDataAndType(Uri.parse(
                "content://com.android.contacts/data/$settingsContactId"),
                ContactsRepository.MIMETYPE_TELEGRAM_CALL
            )
        }
        // Contact from spoken name
        else {
            val contactsRepo = ContactsRepository(context)
            val contacts = contactsRepo.getTelegramCallContacts()
            val contactMatch = contactsRepo
                .findClosestMatchingName(contacts, cleanText) ?: return null
            intent.setDataAndType(Uri.parse(
                "content://com.android.contacts/data/${contactMatch.id}"),
                ContactsRepository.MIMETYPE_TELEGRAM_CALL
            )
        }
        return intent
    }

    private fun getMessageIntent(context: Context,
                                 settingsContactName: String?,
                                 cleanText: String): Intent? {
        // We allow only messages sent if a recipient is set in the settings
        if(settingsContactName == null) {
            return null
        }
        // We need to resolve the phone number separately
        val phoneNumber = ContactsRepository(context).getPhoneNumberByName(settingsContactName)
            ?: return null
        val uri = Uri.parse("tg://resolve?domain=$phoneNumber&text=${Uri.encode(cleanText)}")
        return Intent(Intent.ACTION_VIEW, uri)
    }

}