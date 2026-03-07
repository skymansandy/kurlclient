package dev.skymansandy.kurl.core.di

import dev.skymansandy.kurl.core.KurlEngine
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class KurlCoreModule {

    @Single
    fun kurlEngine(): KurlEngine = KurlEngine()
}