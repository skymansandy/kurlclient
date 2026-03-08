package dev.skymansandy.kurl.core.di

import dev.skymansandy.kurl.core.engine.KurlEngine
import dev.skymansandy.kurl.core.engine.network.createHttpClient
import io.ktor.client.HttpClient
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class KurlCoreModule {

    @Single
    fun httpClient(): HttpClient = createHttpClient()

    @Single
    fun kurlEngine(httpClient: HttpClient): KurlEngine = KurlEngine(
        client = httpClient,
    )
}
