package fi.leif.android.voicecommands.mappers

import android.content.Context
import fi.leif.android.voicecommands.R
import fi.leif.voicecommands.Action
import fi.leif.voicecommands.Action.*
import fi.leif.voicecommands.ParameterKeys
import fi.leif.voicecommands.ParameterKeys.*
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.RtcType

internal class CommandMapper {

    companion object {
        fun getCommandDescription(context: Context, command: Command): String {

            val action = ActionMapper.getActionName(context, command.action)

            when(command.action) {
                GOOGLE_MAPS,WAZE -> {
                    var dest = getParamVal(command, DESTINATION)
                    return dest?.let {
                        dest = String.format(context.getString(R.string.desc_destination), dest)
                        return "$action $dest"
                    } ?: action
                }
                PHONE_CALL -> {
                    var name = getParamVal(command, CONTACT_NAME)
                    return name?.let {
                        name = String.format(context.getString(R.string.desc_to), name)
                        return "$action $name"
                    } ?: action
                }
                TELEGRAM, WHATSAPP -> {
                    var name = getParamVal(command, CONTACT_NAME)
                    return name?.let {
                        var type = command.parametersMap[RTC_TYPE.toString()]
                        if(type == null) type = RtcType.AUDIO_CALL.toString()
                        val rtcType = RtcTypeMapper.getRtcTypeName(context, type)
                        name = String.format(context.getString(R.string.desc_to), name)
                        return "$action - $rtcType $name"
                    } ?: action
                }
                SPOTIFY,NETFLIX -> {
                    var search = getParamVal(command, SEARCH_VALUE)
                    return search?.let {
                        search = String.format(context.getString(R.string.desc_search), search)
                        return "$action $search"
                    } ?: action
                }
                OPEN_APP -> {
                    val appName = getParamVal(command, APP_NAME)
                    return if(appName != null) {
                        "$action "+String.format(context.getString(R.string.desc_package), appName)
                    } else action
                }
                null, Action.UNRECOGNIZED -> throw IllegalArgumentException("Unknown action ${command.action}")
            }
        }

        private fun getParamVal(command: Command, paramKey: ParameterKeys): String? {
            return command.parametersMap[paramKey.toString()]
        }

    }
}
