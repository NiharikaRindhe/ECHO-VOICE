package fi.leif.android.voicecommands.repositories

import android.content.Context
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.util.Log
import org.apache.commons.text.similarity.LevenshteinDistance
import java.util.Locale

    data class Contact(
    val name: String,
    val phone: String,
    val id: String
) {
    override fun toString(): String {
        return name // "$name ($value)"
    }
}

class ContactsRepository (private val context: Context) {

    companion object {
        const val MIMETYPE_PHONE = "vnd.android.cursor.item/phone_v2"
        const val MIMETYPE_WHATSAPP_VOIP = "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
        const val MIMETYPE_TELEGRAM_CALL = "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call"
        const val DEFAULT_ALLOWED_WRONG_CHARS_PER_WORD = 1
    }

    private fun getContacts(mimeType: String, idColumnName: String): List<Contact> {

        val contacts = mutableListOf<Contact>()
        val addedContacts = HashSet<String>()

        val projection = arrayOf(ContactsContract.Data.DISPLAY_NAME,
            Phone.NUMBER,
            idColumnName)
        val selection = "${ContactsContract.Data.MIMETYPE}=? AND ${Phone.NUMBER} IS NOT NULL"
        val uri = ContactsContract.Data.CONTENT_URI
        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            arrayOf(mimeType),
            ContactsContract.Data.DISPLAY_NAME + " ASC")
        (cursor != null && cursor.count > 0).let {
            while (cursor?.moveToNext() == true) { 
                try {
                    val name = cursor.getString(0)
                    val phone = cursor.getString(1).replace(" ", "")
                    val id = cursor.getString(2)
                    if(name != null && !addedContacts.contains(id)) {
                        contacts.add(Contact(name, phone, id))
                        addedContacts.add(id)
                    }
                } catch (e: Exception) {
                    Log.e("getContacts", e.toString(), e)
                }
            }
        }
        cursor?.close()
        return contacts
    }

    fun getPhoneNumberByName(contactName: String): String? {
        val cursor = context.contentResolver.query(
            Phone.CONTENT_URI,
            arrayOf(Phone.NUMBER),
            "${Phone.DISPLAY_NAME} = ?",
            arrayOf(contactName), null
        )
        var phoneNumber: String? = null
        cursor?.use {
            if (it.moveToFirst()) {
                val phoneNumberIndex: Int = it.getColumnIndex(Phone.NUMBER)
                phoneNumber = formatPhoneNumber(it.getString(phoneNumberIndex))
            }
        }
        cursor?.close()
        return phoneNumber
    }

    private fun formatPhoneNumber(phoneNr: String?): String? {
        if(phoneNr == null) return null
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return PhoneNumberUtils.formatNumberToE164(phoneNr, tm.simCountryIso.uppercase())
    }

    fun getPhoneContacts(): List<Contact> {
        // TODO: Caching?
        return getContacts(
            MIMETYPE_PHONE,
            Phone.CONTACT_ID
        )
    }

    fun getWhatsAppVoipContacts(): List<Contact> {
        // TODO: Caching?
        return getContacts(
            MIMETYPE_WHATSAPP_VOIP,
            ContactsContract.Data._ID
        )
    }

    fun getTelegramCallContacts(): List<Contact> {
        // TODO: Caching?
        return getContacts(
            MIMETYPE_TELEGRAM_CALL,
            ContactsContract.Data._ID
        )
    }

    fun findClosestMatchingName(contacts: List<Contact>,
                                name: String,
                                allowedWrongCharsPerWord: Int = DEFAULT_ALLOWED_WRONG_CHARS_PER_WORD):
            Contact? {
        val levDist = LevenshteinDistance.getDefaultInstance()
        val locale = Locale.getDefault()

        var minWrongCharsFound = Int.MAX_VALUE
        var nearestContact: Contact? = null
        val words = name.split(" ")

        // Find closest matching name
        for(contact in contacts) {
            // If multiple names passed, make full match, otherwise match first name only
            val contactName = if(words.size > 1) contact.name
                else contact.name.split(" ")[0]
            val wrongChars = levDist.apply(
                name.lowercase(locale),
                contactName.lowercase(locale)
            )
            // Exact match => return contact
            if(wrongChars == 0) {
                Log.d("findClosestMatchingName", "Exact match $contact")
                return contact
            } else if(wrongChars < minWrongCharsFound) {
                minWrongCharsFound = wrongChars
                nearestContact = contact
                Log.d("findClosestMatchingName",
                    "case MULTIPLE words: Current name = '$name' "+
                            "minWrongCharsFound = $minWrongCharsFound, "+
                            "nearestContact = $nearestContact")

            }
        }
        // Return null if minimum wrong cars found exceeds allowed wrongs chars per word
        val maxAllowedWrongChars = words.size * allowedWrongCharsPerWord
        Log.d(
            "findClosestMatchingName",
            "maxAllowedWrongChars = $maxAllowedWrongChars, "+
                    "minWrongCharsFound = $minWrongCharsFound, "+
                    "nearestContact = $nearestContact"
        )
        return if(minWrongCharsFound <= maxAllowedWrongChars) nearestContact else null
    }
}