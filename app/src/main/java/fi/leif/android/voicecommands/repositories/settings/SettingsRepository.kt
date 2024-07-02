package fi.leif.android.voicecommands.repositories.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import fi.leif.voicecommands.Command
import fi.leif.voicecommands.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Singleton

private const val DATA_STORE_FILE_NAME = "settings.pb"

private val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = SettingsSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { DefaultSettings.get() }
    )
)

@Singleton
class SettingsRepository (private val context: Context) {

    suspend fun getSettings(): Flow<Settings>  {
        return context.settingsDataStore.data
            .catch {
                if (it is IOException) {
                    emit(DefaultSettings.get())
                } else {
                    throw it
                }
            }
    }

    suspend fun setExtraLanguage(extraLanguage: String?) {
        context.settingsDataStore.updateData {
            it.toBuilder().setExtraLanguage(extraLanguage).build()
        }
    }

    suspend fun updateDefaultCommand(command: Command) {
        context.settingsDataStore.updateData {
            it.toBuilder().setDefaultCommand(command).build()
        }
    }
    suspend fun updateCommand(position: Int, command: Command) {
        context.settingsDataStore.updateData {
            it.toBuilder().setCommands(position, command).build()
        }
    }

    suspend fun addCommand(command: Command) {
        context.settingsDataStore.updateData {
            it.toBuilder().addCommands(command).build()
        }
    }

    suspend fun deleteCommand(position: Int) {
        context.settingsDataStore.updateData {
            it.toBuilder().removeCommands(position).build()
        }
    }

    suspend fun setMaxRms(rmsDb: Float) {
        context.settingsDataStore.updateData {
            it.toBuilder().setMaxRms(rmsDb).build()
        }
    }

    suspend fun setRecMaxRms(rmsDb: Float) {
        context.settingsDataStore.updateData {
            it.toBuilder().setRecMaxRms(rmsDb).build()
        }
    }

}