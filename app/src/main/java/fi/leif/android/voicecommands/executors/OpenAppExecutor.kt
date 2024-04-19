package fi.leif.android.voicecommands.executors

import android.content.Context
import android.content.Intent
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys

class OpenAppExecutor : Executor(Action.OPEN_APP) {

    override fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent? {
        val pkg = configCommand.parametersMap[ParameterKeys.APP_PACKAGE.toString()]
        return pkg?.let { context.packageManager.getLaunchIntentForPackage(it) }
    }

}