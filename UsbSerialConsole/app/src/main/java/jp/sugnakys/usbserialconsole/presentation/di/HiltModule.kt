package jp.sugnakys.usbserialconsole.presentation.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.sugnakys.usbserialconsole.usb.UsbService
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application.applicationContext
}

@InstallIn(SingletonComponent::class)
@Module
object ServiceModule {
    @Provides
    @Singleton
    fun provideUsbService(): UsbService {
        return UsbService()
    }
}

@InstallIn(SingletonComponent::class)
@Module
object PreferenceModule {
    @Provides
    @Singleton
    fun providePreference(application: Application): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
    }
}