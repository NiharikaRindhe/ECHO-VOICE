package fi.leif.android.voicecommands.repositories

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class LanguageRepositoryModule {
    @Provides
    fun provideLanguageRepository(@ApplicationContext context: Context): LanguageRepository {
        return LanguageRepository(context)
    }
}