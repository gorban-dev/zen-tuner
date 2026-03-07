package dev.gorban.zentuner

import android.app.Application
import dev.gorban.zentuner.feature.tuner.di.tunerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ZenTunerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ZenTunerApp)
            modules(tunerModule)
        }
    }
}
