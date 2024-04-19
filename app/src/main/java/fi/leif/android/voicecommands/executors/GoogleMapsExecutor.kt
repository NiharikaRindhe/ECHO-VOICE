package fi.leif.android.voicecommands.executors

import android.content.Context
import android.content.Intent
import android.net.Uri
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class GoogleMapsExecutor : DeepLinkExecutor(
        Action.GOOGLE_MAPS,
    "com.google.android.apps.maps",
    "google.navigation:q=") {

    override fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent {
        val txt = getParameterOrText(cleanText, configCommand, ParameterKeys.DESTINATION)
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse(uri + URLEncoder.encode(txt, StandardCharsets.UTF_8.toString()))
        )
    }
}