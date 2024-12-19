//This has everything
//package fi.leif.android.voicecommands.executors
//
//import android.content.ActivityNotFoundException
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.provider.ContactsContract
//import android.telephony.PhoneNumberUtils
//import android.util.Log
//import fi.leif.android.voicecommands.mappers.ActionMapper
//import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
//import fi.leif.voicecommands.Action
//import fi.leif.voicecommands.Command
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import java.net.URLEncoder
//import javax.inject.Inject
//
//class CommandLauncher @Inject constructor(
//    private val context: Context,
//    private val settingsRepository: SettingsRepository
//) {
//
//    private val wordsMap = linkedMapOf<String, Command>()
//    private var defaultCommand: Command = Command.getDefaultInstance()
//
//    init {
//        CoroutineScope(Dispatchers.Main).launch {
//            settingsRepository.getSettings().collect {
//                buildWordsActionsMap(it.commandsList)
//                defaultCommand = it.defaultCommand
//            }
//        }
//    }
//
//    private fun buildWordsActionsMap(commands: List<Command>) {
//        val unsortedMap = HashMap<String, Command>()
//        for (cmd in commands) for (word in cmd.wordsList) {
//            unsortedMap[word] = cmd
//        }
//
//        // Add custom keywords for Google Maps commands
//        unsortedMap["take me to"] = Command.newBuilder().setAction(Action.GOOGLE_MAPS).build()
//        unsortedMap["show maps for"] = Command.newBuilder().setAction(Action.GOOGLE_MAPS).build()
//        unsortedMap["navigate me to"] = Command.newBuilder().setAction(Action.GOOGLE_MAPS).build()
//
//        // Add custom keywords for WhatsApp messaging
//        unsortedMap["whatsapp"] = Command.newBuilder().setAction(Action.WHATSAPP).build()
//        unsortedMap["text"] = Command.newBuilder().setAction(Action.WHATSAPP).build()
//        unsortedMap["message"] = Command.newBuilder().setAction(Action.WHATSAPP).build()
//
//
//        // Create a Map of all command words sorted by longest word first
//        unsortedMap.entries.sortedByDescending { it.key.length }.forEach { (word, command) ->
//            wordsMap[word] = command
//        }
//    }
//
//    fun textContainsCommand(text: String): String? {
//        val lowerText = text.lowercase()
//        if (lowerText.startsWith("open ")) {
//            return "open"
//        }
//
//        wordsMap.entries.forEach { (word, _) ->
//            if (lowerText.contains(word.lowercase())) {
//                return word
//            }
//        }
//        return null
//    }
//
//    suspend fun executeCommand(context: Context, cmdWord: String?, text: String) {
//        val command = wordsMap[cmdWord]
//        try {
//            if (command != null) {
//                if (command.action == Action.GOOGLE_MAPS) {
//                    // Handle Google Maps Commands
//                    val location = text.removePrefix(cmdWord ?: "").trim()
//                    openGoogleMaps(context, location)
//                } else if (command.action == Action.WHATSAPP) {
//                    // Handle WhatsApp messaging: "message [contact name] [message]"
//                    val messageDetails = text.removePrefix("message").trim()
//                    val (contactName, message) = extractContactAndMessage(messageDetails)
//                    sendWhatsAppMessage(context, contactName, message)
//                } else {
//                    // Execute other predefined commands
//                    ActionMapper.getActionExecutor(command.action)
//                        .execute(context, text, command)
//                }
//            } else if (text.startsWith("play", ignoreCase = true) && text.contains("on youtube", ignoreCase = true)) {
//                // Handle YouTube playback
//                val videoName = text.removePrefix("play").removeSuffix("on youtube").trim()
//                playVideoOnYouTube(context, videoName)
//            } else if (text.startsWith("youtube", ignoreCase = true)) {
//                // Handle YouTube playback
//                val videoName = text.removePrefix("youtube").trim()
//                playVideoOnYouTube(context, videoName)
//            } else {
//                // Fall back to dynamic app opening if no predefined command matches
//                val openAppExecutor = OpenAppExecutor()
//                openAppExecutor.execute(context, text, defaultCommand)
//            }
//        } catch (e: ActivityNotFoundException) {
//            Log.e("CommandLauncher", "Activity not found for the command: $text", e)
//        }
//    }
//
//    private fun extractContactAndMessage(text: String): Pair<String, String> {
//        // This function will extract the contact name and message from the text.
//        val parts = text.split(" ", limit = 2)
//        return if (parts.size >= 2) {
//            val contactName = parts[0]
//            val message = parts[1]
//            Pair(contactName, message)
//        } else {
//            Pair("", "")
//        }
//    }
//
//    private fun sendWhatsAppMessage(context: Context, contactName: String, message: String) {
//        try {
//            // Query contacts for matching name
//            val contactPhone = getContactPhoneNumber(context, contactName)
//
//            if (contactPhone != null) {
//                // Send the message using WhatsApp
//                val sendIntent = Intent(Intent.ACTION_VIEW)
//                val uri = Uri.parse("https://wa.me/$contactPhone?text=${Uri.encode(message)}")
//                sendIntent.setData(uri)
//                sendIntent.setPackage("com.whatsapp")
//                context.startActivity(sendIntent)
//            } else {
//                Log.e("sendWhatsAppMessage", "Contact not found for $contactName")
//            }
//        } catch (e: Exception) {
//            Log.e("sendWhatsAppMessage", "Error sending message via WhatsApp", e)
//        }
//    }
//
//    private fun getContactPhoneNumber(context: Context, contactName: String): String? {
//        // Query contacts to find the phone number of the contact
//        val cursor = context.contentResolver.query(
//            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
//            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
//            arrayOf("%$contactName%"),
//            null
//        )
//
//        cursor?.use {
//            while (it.moveToNext()) {
//                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
//                val phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                if (name != null && phoneNumber != null) {
//                    return PhoneNumberUtils.formatNumberToE164(phoneNumber, "US") // Modify country code if needed
//                }
//            }
//        }
//        return null
//    }
//
//    private fun openGoogleMaps(context: Context, location: String) {
//        try {
//            // Construct the Google Maps navigation intent
//            val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(location)}")
//            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//            mapIntent.setPackage("com.google.android.apps.maps") // Open in Google Maps app if installed
//            context.startActivity(mapIntent)
//        } catch (e: ActivityNotFoundException) {
//            // Fallback to browser if Google Maps app is not installed
//            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(location)}"))
//            context.startActivity(browserIntent)
//        }
//    }
//
//    private fun playVideoOnYouTube(context: Context, videoName: String) {
//        try {
//            // Construct the YouTube search query
//            val searchQuery = "https://www.youtube.com/results?search_query=${URLEncoder.encode(videoName, "UTF-8")}"
//
//            // Create an intent to open YouTube with the search query
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchQuery))
//            intent.putExtra("query", videoName)
//            intent.setPackage("com.google.android.youtube") // Opens the YouTube app if installed
//            context.startActivity(intent)
//        } catch (e: Exception) {
//            // Fallback to browser if YouTube app is not installed
//            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${URLEncoder.encode(videoName, "UTF-8")}"))
//            context.startActivity(browserIntent)
//        }
//    }
//}

//SPOTIFY
package fi.leif.android.voicecommands.executors

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.util.Log
import fi.leif.android.voicecommands.mappers.ActionMapper
import fi.leif.android.voicecommands.repositories.settings.SettingsRepository
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

class CommandLauncher @Inject constructor(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    private val wordsMap = linkedMapOf<String, Command>()
    private var defaultCommand: Command = Command.getDefaultInstance()

    init {
        CoroutineScope(Dispatchers.Main).launch {
            settingsRepository.getSettings().collect {
                buildWordsActionsMap(it.commandsList)
                defaultCommand = it.defaultCommand
            }
        }
    }

    private fun buildWordsActionsMap(commands: List<Command>) {
        val unsortedMap = HashMap<String, Command>()
        for (cmd in commands) for (word in cmd.wordsList) {
            unsortedMap[word] = cmd
        }

        // Add custom keywords for Google Maps commands
        unsortedMap["take me to"] = Command.newBuilder().setAction(Action.GOOGLE_MAPS).build()
        unsortedMap["show maps for"] = Command.newBuilder().setAction(Action.GOOGLE_MAPS).build()
        unsortedMap["navigate me to"] = Command.newBuilder().setAction(Action.GOOGLE_MAPS).build()

        // Add custom keywords for WhatsApp messaging
        unsortedMap["whatsapp"] = Command.newBuilder().setAction(Action.WHATSAPP).build()
        unsortedMap["text"] = Command.newBuilder().setAction(Action.WHATSAPP).build()
        unsortedMap["message"] = Command.newBuilder().setAction(Action.WHATSAPP).build()

        // Add custom keywords for Spotify
        unsortedMap["play on spotify"] = Command.newBuilder().setAction(Action.SPOTIFY).build()

        // Create a Map of all command words sorted by longest word first
        unsortedMap.entries.sortedByDescending { it.key.length }.forEach { (word, command) ->
            wordsMap[word] = command
        }
    }

    fun textContainsCommand(text: String): String? {
        val lowerText = text.lowercase()
        if (lowerText.startsWith("open ")) {
            return "open"
        }

        wordsMap.entries.forEach { (word, _) ->
            if (lowerText.contains(word.lowercase())) {
                return word
            }
        }
        return null
    }

    suspend fun executeCommand(context: Context, cmdWord: String?, text: String) {
        val command = wordsMap[cmdWord]
        try {
            if (command != null) {
                if (command.action == Action.GOOGLE_MAPS) {
                    // Handle Google Maps Commands
                    val location = text.removePrefix(cmdWord ?: "").trim()
                    openGoogleMaps(context, location)
                } else if (command.action == Action.WHATSAPP) {
                    // Handle WhatsApp messaging: "message [contact name] [message]"
                    val messageDetails = text.removePrefix("message").trim()
                    val (contactName, message) = extractContactAndMessage(messageDetails)
                    sendWhatsAppMessage(context, contactName, message)
                } else if (command.action == Action.SPOTIFY) {
                    // Handle Spotify playback
                    val songName = text
                        .lowercase()
                        .substringAfter("play", "")
                        .substringBefore("on spotify", "")
                        .trim()
                    if (songName.isNotEmpty()) {
                        playSongOnSpotify(context, songName)
                    } else {
                        Log.e("CommandLauncher", "Song name could not be extracted from: $text")
                    }
                } else {
                    // Execute other predefined commands
                    ActionMapper.getActionExecutor(command.action)
                        .execute(context, text, command)
                }
            } else if (text.startsWith("play", ignoreCase = true) && text.contains("on youtube", ignoreCase = true)) {
                // Handle YouTube playback
                val videoName = text
                    .lowercase()
                    .substringAfter("play", "")
                    .substringBefore("on youtube", "")
                    .trim()
                playVideoOnYouTube(context, videoName)
            } else if (text.startsWith("youtube", ignoreCase = true)) {
                // Handle YouTube playback
                val videoName = text
                    .lowercase()
                    .substringAfter("youtube", "")
                    .trim()
                playVideoOnYouTube(context, videoName)
            } else {
                // Fall back to dynamic app opening if no predefined command matches
                val openAppExecutor = OpenAppExecutor()
                openAppExecutor.execute(context, text, defaultCommand)
            }
        } catch (e: ActivityNotFoundException) {
            Log.e("CommandLauncher", "Activity not found for the command: $text", e)
        }
    }

    private fun extractContactAndMessage(text: String): Pair<String, String> {
        // This function will extract the contact name and message from the text.
        val parts = text.split(" ", limit = 2)
        return if (parts.size >= 2) {
            val contactName = parts[0]
            val message = parts[1]
            Pair(contactName, message)
        } else {
            Pair("", "")
        }
    }

    private fun sendWhatsAppMessage(context: Context, contactName: String, message: String) {
        try {
            // Query contacts for matching name
            val contactPhone = getContactPhoneNumber(context, contactName)

            if (contactPhone != null) {
                // Send the message using WhatsApp
                val sendIntent = Intent(Intent.ACTION_VIEW)
                val uri = Uri.parse("https://wa.me/$contactPhone?text=${Uri.encode(message)}")
                sendIntent.setData(uri)
                sendIntent.setPackage("com.whatsapp")
                context.startActivity(sendIntent)
            } else {
                Log.e("sendWhatsAppMessage", "Contact not found for $contactName")
            }
        } catch (e: Exception) {
            Log.e("sendWhatsAppMessage", "Error sending message via WhatsApp", e)
        }
    }

    private fun getContactPhoneNumber(context: Context, contactName: String): String? {
        // Query contacts to find the phone number of the contact
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$contactName%"),
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                if (name != null && phoneNumber != null) {
                    return PhoneNumberUtils.formatNumberToE164(phoneNumber, "US") // Modify country code if needed
                }
            }
        }
        return null
    }

    private fun openGoogleMaps(context: Context, location: String) {
        try {
            // Construct the Google Maps navigation intent
            val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(location)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps") // Open in Google Maps app if installed
            context.startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            // Fallback to browser if Google Maps app is not installed
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(location)}"))
            context.startActivity(browserIntent)
        }
    }

    private fun playVideoOnYouTube(context: Context, videoName: String) {
        try {
            // Construct the YouTube search query
            val searchQuery = "https://www.youtube.com/results?search_query=${URLEncoder.encode(videoName, "UTF-8")}"

            // Create an intent to open YouTube with the search query
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchQuery))
            intent.putExtra("query", videoName)
            intent.setPackage("com.google.android.youtube") // Opens the YouTube app if installed
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser if YouTube app is not installed
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${URLEncoder.encode(videoName, "UTF-8")}"))
            context.startActivity(browserIntent)
        }
    }

    private fun playSongOnSpotify(context: Context, songName: String) {
        try {
            // Construct the Spotify URI or search query
            val searchQuery = "spotify:search:${URLEncoder.encode(songName, "UTF-8")}"

            // Create an intent to open Spotify with the search query
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchQuery))
            intent.setPackage("com.spotify.music") // Opens the Spotify app if installed
            context.startActivity(intent)
        } catch (e: Exception) {
            // Log the error or fallback logic
            Log.e("CommandLauncher", "Error launching Spotify: $e")
        }
    }
}
