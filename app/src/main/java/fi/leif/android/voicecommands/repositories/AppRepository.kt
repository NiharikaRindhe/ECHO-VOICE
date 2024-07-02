package fi.leif.android.voicecommands.repositories

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


data class App(
    val name: String,
    val pkg: String
) {
    override fun toString(): String {
        return name
    }
}

class AppRepository(private val context: Context)
{
    suspend fun getApplications(): List<App> {
        return withContext(Dispatchers.IO) {

            val list = ArrayList<App>()
            val launcherApps = context.getSystemService(LauncherApps::class.java)
            val activityList = launcherApps.getActivityList(null, Process.myUserHandle())
            for (activity in activityList) {
                val appName = activity.label.toString()
                val pkg = activity.applicationInfo.packageName
                list.add(App(appName, pkg))
            }
            return@withContext list.sortedBy { it.name }
        }
    }

    fun isActivityAvailable(packageName: String, activityClassName: String): Boolean {
        val pm: PackageManager = context.packageManager
        try {
            val packageInfo: PackageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            for (activity in packageInfo.activities) {
                if (activity.name == activityClassName) {
                    return true
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return false
    }
}