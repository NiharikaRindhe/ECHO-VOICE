package fi.leif.android.voicecommands.repositories

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.lifecycle.MutableLiveData

const val KEY_SUPPORTED_LANGUAGE_NAMES = "android.speech.extra.SUPPORTED_LANGUAGE_NAMES"

class LanguageRepository(val context: Context) {

    private val languageNames = MutableLiveData<List<String>>()
    private val languageCodes = mutableListOf<String>()

    fun getSupportedLanguages(): MutableLiveData<List<String>> {
        val broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {
                try {
                    val result = getResultExtras(true)
                    val langCodes =
                        result?.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)
                    val langNames =
                        result?.getStringArrayList(KEY_SUPPORTED_LANGUAGE_NAMES)

                    if (langCodes.isNullOrEmpty()) {
                        Log.e("LanguageRepository", "Language codes are empty. Using fallback.")
                        setFallbackLanguages()
                        return
                    }

                    // Allowed languages
                    val allowedLangCodes = listOf("en-US", "hi-IN")
                    val allowedLangNames = listOf("English (United States)", "Hindi (India)")
                    val names = mutableListOf<String>()

                    langCodes.forEachIndexed { index, langCode ->
                        if (allowedLangCodes.contains(langCode)) {
                            val langName = langNames?.getOrNull(index) ?: langCode
                            names.add(langName)
                            languageCodes.add(langCode)
                        }
                    }

                    if (names.isEmpty()) {
                        Log.w("LanguageRepository", "No allowed languages found. Using fallback.")
                        setFallbackLanguages()
                    } else {
                        languageNames.postValue(names)
                    }

                } catch (e: Exception) {
                    Log.e("LanguageRepository", "Error processing language broadcast", e)
                    setFallbackLanguages()
                }
            }
        }

        val langIntent = Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS)
        langIntent.setPackage("com.google.android.googlequicksearchbox")

        try {
            context.sendOrderedBroadcast(
                langIntent, null, broadCastReceiver, null,
                Activity.RESULT_OK, null, null
            )
        } catch (e: Exception) {
            Log.e("LanguageRepository", "Error sending language broadcast. Using fallback.", e)
            setFallbackLanguages()
        }

        return languageNames
    }

    private fun setFallbackLanguages() {
        languageNames.postValue(listOf("English (United States)", "Hindi (India)"))
        languageCodes.clear()
        languageCodes.addAll(listOf("en-US", "hi-IN"))
    }

    fun getIndexByLanguageCode(langCode: String?): Int {
        val index = langCode?.let { languageCodes.indexOf(it) } ?: -1
        return if (index >= 0) index else 0 // Fallback to the first index (default language)
    }

    fun getLanguageCodeByIndex(index: Int?): String {
        return languageCodes.getOrNull(index ?: 0) ?: "en-US"
    }
}
