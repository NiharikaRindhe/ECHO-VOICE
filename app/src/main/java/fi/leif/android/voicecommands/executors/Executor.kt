package fi.leif.android.voicecommands.executors

import android.content.Context
import android.content.Intent
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys

abstract class Executor(val action: Action, val packageName: String? = null) {

    suspend fun execute(context: Context, spokenText: String, configCommand: Command) {
        // Clean up command words
        val cleanText = removeCommandWords(spokenText.lowercase(),configCommand)
        if(packageName == null)
            executeWithoutPackageName(context, cleanText, configCommand)
        else
            executeWithPackageName(context, cleanText, configCommand)
    }

    private suspend fun executeWithPackageName(context: Context, cleanText: String, configCommand: Command) {
        // No spoken text nor configured parameters => just open intent by package name
        // TODO: Maybe move this logic into sub executors instead..
        if(cleanText.isEmpty() && configCommand.parametersMap.isNullOrEmpty()) {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName!!)
            intent?.let {
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        }
        // Use sub executor to fetch Intent
        else {
            val intent = getIntent(context, cleanText, configCommand)
            intent?.let {
                it.setPackage(packageName)
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        }
    }

    private suspend fun executeWithoutPackageName(context: Context, cleanText: String,
                                          configCommand: Command) {
        val intent = getIntent(context, cleanText, configCommand)
        intent?.let {
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }

    private fun removeCommandWords(text: String, configCommand: Command): String {
        var txt = text
        for(word in configCommand.wordsList) { txt = txt.replace(word.lowercase(),"") }
        return txt.trim().replace(" +", " ")
    }

    abstract suspend fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent?

    protected fun getParameter(command: Command, key: ParameterKeys): String? {
        return command.parametersMap[key.toString()]
    }

    protected fun getParameterOrText(text: String, command: Command, key: ParameterKeys): String {
        return command.parametersMap[key.toString()] ?: text
    }
}

abstract class DeepLinkExecutor(action: Action, packageName: String, val uri: String):
    Executor(action, packageName)
