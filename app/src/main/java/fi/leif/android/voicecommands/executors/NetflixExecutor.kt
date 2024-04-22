package fi.leif.android.voicecommands.executors

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import fi.leif.android.voicecommands.repositories.AppRepository
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.ParameterKeys

class NetflixExecutor : Executor(Action.NETFLIX, "com.netflix.mediaclient") {

    companion object {
        const val SEARCH_ACTIVITY = "com.netflix.mediaclient.ui.search.SearchActivity"
        const val UI_ACTIVITY = "com.netflix.mediaclient.ui.launch.UIWebViewActivity"
    }
    override fun getIntent(context: Context, cleanText: String, configCommand: Command): Intent {
        val txt = getParameterOrText(cleanText, configCommand, ParameterKeys.SEARCH_VALUE)
        // Unfortunately search activity seems to be removed in later Netflix versions :(
        return if(AppRepository(context).isActivityAvailable(packageName!!, SEARCH_ACTIVITY)) {
            val intent = Intent(Intent.ACTION_SEARCH)
            intent.putExtra(SearchManager.QUERY, txt)
            intent.setClassName(packageName,SEARCH_ACTIVITY)
            intent
        }
        // TODO - figure out if there are alternative ways for searching a title
        else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setClassName(packageName,UI_ACTIVITY)
            intent
        }
    }
}