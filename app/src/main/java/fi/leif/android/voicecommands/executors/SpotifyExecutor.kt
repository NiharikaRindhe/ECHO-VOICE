//package fi.leif.android.voicecommands.executors
//
//import android.app.SearchManager
//import android.content.Context
//import android.content.Intent
//import android.provider.MediaStore
//import fi.leif.voicecommands.Action
//import fi.leif.voicecommands.Command
//import fi.leif.voicecommands.ParameterKeys
//
//class SpotifyExecutor : Executor(Action.SPOTIFY, "com.spotify.music") {
//    override suspend fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent {
//        val txt = getParameterOrText(cleanText, configCommand, ParameterKeys.SEARCH_VALUE)
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
//        intent.putExtra(SearchManager.QUERY, txt)
//        return intent
//    }
//}

//Spotify seach fix
package fi.leif.android.voicecommands.executors

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys

class SpotifyExecutor : Executor(Action.SPOTIFY, "com.spotify.music") {
    override suspend fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent {
        // Extract text parameter or fallback to cleanText
        val txt = getParameterOrText(cleanText, configCommand, ParameterKeys.SEARCH_VALUE).trim()

        // Build Spotify search URI
        val searchQuery = "spotify:search:${Uri.encode(txt)}"

        // Create and configure intent for Spotify
        return Intent(Intent.ACTION_VIEW, Uri.parse(searchQuery)).apply {
            setPackage("com.spotify.music")
        }
    }
}
