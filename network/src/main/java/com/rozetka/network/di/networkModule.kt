package com.rozetka.network.di

import com.rozetka.network.ApiMethods
import com.rozetka.network.Methods
import com.rozetka.network.MospolytechMethods
import com.rozetka.network.campus.CampusApi
import com.rozetka.network.provideHttpClient
import com.rozetka.network.provideHttpClientCampus
import com.rozetka.network.provideUnsecureHttpClient
import org.koin.dsl.module

val networkModule = module {
    single { provideHttpClient() }
    single { provideHttpClientCampus() }
    single { provideUnsecureHttpClient() }
    single<Methods> { ApiMethods() }
    single { ApiMethods() }
    single { CampusApi() }
    single { MospolytechMethods() }
}