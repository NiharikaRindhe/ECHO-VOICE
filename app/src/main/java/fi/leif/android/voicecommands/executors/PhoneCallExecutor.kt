package fi.leif.android.voicecommands.executors

import android.content.Context
import android.content.Intent
import android.net.Uri
import fi.leif.android.voicecommands.repositories.ContactsRepository
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys

class PhoneCallExecutor : Executor(Action.OPEN_APP) {
    override fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent? {
        // Settings contact  number
        val settingsTelNr = getParameter(configCommand,ParameterKeys.CONTACT_PHONE)
        return if(settingsTelNr != null) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:$settingsTelNr"))
        }
        // Attempt to resolve contact by spoken name
        else {
            val contactsRepo = ContactsRepository(context)
            val contacts = contactsRepo.getPhoneContacts()
            val contactMatch = contactsRepo.findClosestMatchingName(contacts, cleanText)

            if(contactMatch == null) null
            else Intent(Intent.ACTION_CALL, Uri.parse("tel:${contactMatch.phone}"))
        }
    }
}