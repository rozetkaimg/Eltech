package com.rozetka.epolitech

import android.app.Application
import com.rozetka.data.di.dataModule
import com.rozetka.domain.di.domainModule
import com.rozetka.network.di.networkModule
import com.rozetka.presentation.di.presentationModule
import com.rozetka.storage.di.storageModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class EPolitechApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@EPolitechApplication)
            modules(
                networkModule,
                domainModule,
                dataModule,
                storageModule,
                presentationModule
            )
        }
    }

}