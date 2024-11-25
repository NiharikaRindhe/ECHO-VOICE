package fi.leif.android.voicecommands.executors

import android.content.Context
import android.content.Intent
import android.util.Log
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.android.voicecommands.repositories.AppRepository

class OpenAppExecutor : Executor(Action.OPEN_APP) {

    override suspend fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent? {
        val packageManager = context.packageManager
        val appRepository = AppRepository(context)

        // Remove "open" from the command text if it exists
        val cleanAppName = cleanText.removePrefix("open").trim()

        // Get the list of installed apps
        val apps = appRepository.getApplications()

        // Attempt to find an app whose name matches the spoken text
        val matchedApp = apps.find { app ->
            app.name.equals(cleanAppName, ignoreCase = true) ||
                    app.name.contains(cleanAppName, ignoreCase = true)
        }

        // Log the result of the matching process
        Log.d("OpenAppExecutor", "Matched App: $matchedApp for spoken text: $cleanAppName")

        // If a match is found, return the intent to launch the app
        return matchedApp?.let { packageManager.getLaunchIntentForPackage(it.pkg) }
    }
}
