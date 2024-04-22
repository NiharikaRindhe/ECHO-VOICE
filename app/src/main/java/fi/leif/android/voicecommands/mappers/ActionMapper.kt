package fi.leif.android.voicecommands.mappers

import android.content.Context
import fi.leif.android.voicecommands.R
import fi.leif.android.voicecommands.executors.Executor
import fi.leif.android.voicecommands.executors.GoogleMapsExecutor
import fi.leif.android.voicecommands.executors.NetflixExecutor
import fi.leif.android.voicecommands.executors.OpenAppExecutor
import fi.leif.android.voicecommands.executors.PhoneCallExecutor
import fi.leif.android.voicecommands.executors.SpotifyExecutor
import fi.leif.android.voicecommands.executors.TelegramExecutor
import fi.leif.android.voicecommands.executors.WazeExecutor
import fi.leif.android.voicecommands.executors.WhatsAppExecutor
import fi.leif.android.voicecommands.view.fragments.actions.ActionFragment
import fi.leif.android.voicecommands.view.fragments.actions.MediaFragment
import fi.leif.android.voicecommands.view.fragments.actions.NavFragment
import fi.leif.android.voicecommands.view.fragments.actions.NoneFragment
import fi.leif.android.voicecommands.view.fragments.actions.OpenAppFragment
import fi.leif.android.voicecommands.view.fragments.actions.PhoneCallFragment
import fi.leif.android.voicecommands.view.fragments.actions.TelegramFragment
import fi.leif.android.voicecommands.view.fragments.actions.WhatsAppFragment
import fi.leif.voicecommands.Action.*
import fi.leif.voicecommands.Action

class ActionMapper {

    companion object {

        private val actions = listOf(
            NONE,
            GOOGLE_MAPS,
            WAZE,
            PHONE_CALL,
            TELEGRAM,
            WHATSAPP,
            SPOTIFY,
            NETFLIX,
            OPEN_APP
        )

        fun getActionName(context: Context, action: Action): String {
            val resourceId: Int = when(action) {
                NONE -> R.string.action_none
                GOOGLE_MAPS -> R.string.action_google_maps
                WAZE -> R.string.action_waze
                PHONE_CALL -> R.string.action_phone_call
                TELEGRAM -> R.string.action_telegram
                WHATSAPP -> R.string.action_whatsapp
                SPOTIFY -> R.string.action_spotify
                NETFLIX -> R.string.action_netflix
                OPEN_APP -> R.string.action_open_app
                UNRECOGNIZED -> R.string.unrecognized
            }
            return context.getString(resourceId)
        }

        fun getAllActionNames(context: Context, showNoAction: Boolean = false): Array<String> {
            return actions
                .filter {
                    if(it == NONE && showNoAction == false) false else true
                }
                .map { action -> getActionName(context, action) }
                .toTypedArray()
        }

        fun getActionAt(position: Int): Action {
            return actions[position]
        }

        fun getActionFragment(action: Action): ActionFragment {
            return when(action) {
                NONE -> NoneFragment()
                TELEGRAM -> TelegramFragment()
                WHATSAPP -> WhatsAppFragment()
                PHONE_CALL -> PhoneCallFragment()
                OPEN_APP -> OpenAppFragment()
                WAZE -> NavFragment()
                GOOGLE_MAPS -> NavFragment()
                SPOTIFY -> MediaFragment()
                NETFLIX -> MediaFragment()
                else -> throw IllegalArgumentException("Unknown action '${action.toString()}'")
            }
        }

        fun getActionExecutor(action: Action): Executor {
            return when(action) {
                TELEGRAM -> TelegramExecutor()
                WHATSAPP -> WhatsAppExecutor()
                PHONE_CALL -> PhoneCallExecutor()
                OPEN_APP -> OpenAppExecutor()
                WAZE -> WazeExecutor()
                GOOGLE_MAPS -> GoogleMapsExecutor()
                SPOTIFY -> SpotifyExecutor()
                NETFLIX -> NetflixExecutor()
                else -> throw IllegalArgumentException("Unknown action '${action.toString()}'")
            }

        }
    }
}