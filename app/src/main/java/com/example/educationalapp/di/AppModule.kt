package com.example.educationalapp.di

import android.content.Context
import com.example.educationalapp.di.SoundManager
import com.example.educationalapp.di.BgMusicManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSoundManager(
        @ApplicationContext context: Context,
        bgMusicManager: BgMusicManager
    ): SoundManager {
        // Inject the BgMusicManager into the SoundManager so that effects can
        // automatically duck the background music when played.
        return SoundManager(context, bgMusicManager)
    }

    @Provides
    @Singleton
    fun provideBgMusicManager(@ApplicationContext context: Context): BgMusicManager {
        return BgMusicManager(context)
    }
}