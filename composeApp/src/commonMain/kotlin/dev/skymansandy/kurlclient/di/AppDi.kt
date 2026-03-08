package dev.skymansandy.kurlclient.di

import dev.skymansandy.kurl.core.di.KurlCoreModule
import dev.skymansandy.kurl.store.di.KurlStoreModule
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

fun initKoin() {
    startKoin {
        modules(
            KurlCoreModule().module,
            KurlStoreModule().module,
            WorkspaceModule().module,
        )
    }
}
