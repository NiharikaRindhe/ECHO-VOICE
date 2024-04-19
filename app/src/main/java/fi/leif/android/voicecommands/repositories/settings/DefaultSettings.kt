package fi.leif.android.voicecommands.repositories.settings

import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys
import fi.leif.voicecommands.Settings

object DefaultSettings {
    fun get(): Settings {
        return Settings.newBuilder()
            .setExtraLanguage("sv-SE") // Should it be "sv_SE" ?
            .setMaxRms(10f)
            .addCommands(getCommand("maps", Action.GOOGLE_MAPS))
            .addCommands(getCommand("drive", Action.WAZE))
            .addCommands(getCommand("netflix", Action.NETFLIX))
            .addCommands(getCommand("spotify", Action.SPOTIFY))
            .addCommands(getCommand("call", Action.PHONE_CALL))
            .addCommands(getCommand("whatsapp", Action.WHATSAPP))
            /*
            .addCommands(getCommand("home", Action.OPEN_APP,
                "Agama","altergames.carlauncher"))
            .addCommands(getCommand("radio", Action.OPEN_APP,
                "Radio", "com.spd.spdradio"))
             */
            .build()
    }
    private fun getCommand(words: String, action: Action,
                           appName: String? = null, appPkg: String? = null): Command {
        val builder: Command.Builder =
            Command.newBuilder()
                .addWords(words)
                .setAction(action)
        if(appName != null) {
            builder.putParameters(ParameterKeys.APP_NAME.toString(), appName)
        }
        if(appPkg != null) {
            builder.putParameters(ParameterKeys.APP_PACKAGE.toString(), appPkg)
        }
        return builder.build()
    }
}