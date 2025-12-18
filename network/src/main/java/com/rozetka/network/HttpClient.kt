package com.rozetka.network


import com.rozetka.network.ext.Network.politechApiURL
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.net.ssl.HostnameVerifier


fun provideHttpClient(): HttpClient = HttpClient(Android) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    defaultRequest {
        url("https://e.mospolytech.ru/old/index.php")
    }
    engine {
        sslManager = { httpsURLConnection ->
            httpsURLConnection.sslSocketFactory = Tls12SocketFactory.get()
            httpsURLConnection.hostnameVerifier = HostnameVerifier { _, _ -> true }
        }
    }
}
fun provideHttpClientCampus(): HttpClient = HttpClient(Android) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    defaultRequest {
        url("https://api.campus.dev.dewish.ru")
    }

}

fun provideUnsecureHttpClient(): HttpClient = HttpClient(Android) {
    engine {
        sslManager = { httpsURLConnection ->
            httpsURLConnection.sslSocketFactory = Tls12SocketFactory.get()
            httpsURLConnection.hostnameVerifier = HostnameVerifier { _, _ -> true }
        }
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            clearIgnoredTypes()
        })
    }
    defaultRequest {
        url(politechApiURL)
    }


}
fun provideUnsecureHttpClientClean(): HttpClient = HttpClient(Android) {
    engine {
        sslManager = { httpsURLConnection ->
            httpsURLConnection.sslSocketFactory = Tls12SocketFactory.get()
            httpsURLConnection.hostnameVerifier = HostnameVerifier { _, _ -> true }
        }
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}