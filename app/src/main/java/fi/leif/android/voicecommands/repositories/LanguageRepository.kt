package fi.leif.android.voicecommands.repositories

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.lifecycle.MutableLiveData

const val KEY_SUPPORTED_LANGUAGE_NAMES = "android.speech.extra.SUPPORTED_LANGUAGE_NAMES"

class LanguageRepository(val context: Context) {

    private val languageNames = MutableLiveData<List<String>>()
    private val languageCodes = ArrayList<String>()

    fun getSupportedLanguages(): MutableLiveData<List<String>> {

        val broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                val result = getResultExtras(true)
                val langCodes =
                    result.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)
                val langNames = result.getStringArrayList(KEY_SUPPORTED_LANGUAGE_NAMES)
                val names = ArrayList<String>()
                langCodes?.forEachIndexed { index, langCode ->
                    val langName = langNames?.get(index) ?: langCode
                    names.add(langName)
                    languageCodes.add(langCode)
                }
                languageNames.value = names
            }
        }

        val langIntent = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)
        langIntent.setPackage("com.google.android.googlequicksearchbox")
        context.sendOrderedBroadcast(langIntent, null, broadCastReceiver, null,
            Activity.RESULT_OK, null, null)

        return languageNames
    }

    fun getIndexByLanguageCode(langCode: String?): Int {
        return langCode?.let { languageCodes.indexOf(langCode) } ?: 0
    }

    fun getLanguageCodeByIndex(index: Int?): String {
        return languageCodes[index!!]
    }
}