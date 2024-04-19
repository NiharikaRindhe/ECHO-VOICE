package fi.leif.android.voicecommands.repositories.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import fi.leif.voicecommands.Settings
import java.io.InputStream
import java.io.OutputStream

object SettingsSerializer : Serializer<Settings> {

    override val defaultValue: Settings = DefaultSettings.get()

    override suspend fun readFrom(input: InputStream): Settings {
        try {
            return Settings.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        t.writeTo(output)
    }

}
