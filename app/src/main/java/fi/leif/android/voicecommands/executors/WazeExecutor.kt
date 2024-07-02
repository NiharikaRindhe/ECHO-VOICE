package fi.leif.android.voicecommands.executors

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Suppress("DEPRECATION")
class WazeExecutor: DeepLinkExecutor(
    Action.WAZE,
    "com.waze",
    "https://waze.com/ul?navigate=yes") {

    override suspend fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent {
        val txt = getParameterOrText(cleanText, configCommand, ParameterKeys.DESTINATION)
        val (latitude,longitude) = getLocation(context, txt)
        // Unable to determine latitude/longitude, make a search by address
        return if(latitude == null || longitude == null) {
            Intent(Intent.ACTION_VIEW,
                Uri.parse(
                    "$uri&q=" + withContext(Dispatchers.IO) {
                        URLEncoder.encode(
                            txt, StandardCharsets.UTF_8.toString()
                        )
                    }
                )
            )
        }
        // Use latitude/longitude
        else {
            val dest = "$uri&ll=$latitude%2C$longitude"
            Intent(Intent.ACTION_VIEW, Uri.parse(dest))
        }
    }

    private fun getLocation(context: Context, address: String): Pair<Double?,Double?> {
        val geocoder = Geocoder(context)
        // Deprecated getFromLocationName() - but we can't use a listener due to minSdk=28..
        val list = geocoder.getFromLocationName(address, 1)
        return if(list.isNullOrEmpty()) {
            Pair(null,null)
        } else {
            Pair(list[0].latitude, list[0].longitude)
        }
    }
}