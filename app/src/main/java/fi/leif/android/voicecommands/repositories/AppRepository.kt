package fi.leif.android.voicecommands.repositories

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageInfo
import android.content.pm.PackageManager


data class App(
    val name: String,
    val pkg: String
)

class AppRepository(private val context: Context)
{
    fun getApplications(): List<App> {
        val list = ArrayList<App>()
        val launcherApps = context.getSystemService(LauncherApps::class.java)
        val activityList = launcherApps.getActivityList(null, android.os.Process.myUserHandle())
        for(activity in activityList) {
            val appName = activity.label.toString()
            val pkg = activity.applicationInfo.packageName
            list.add(App(appName, pkg))
        }
        return list.sortedBy { it.name }
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