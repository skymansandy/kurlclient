package dev.skymansandy.kurl.store.di

import dev.skymansandy.kurl.store.impl.KurlStoreImpl
import dev.skymansandy.kurl.store.api.KurlStore
import dev.skymansandy.kurl.store.impl.data.db.createDatabaseDriver
import dev.skymansandy.kurlstore.db.KurlDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class KurlStoreModule {

    @Single
    fun kurlDatabase(): KurlDatabase = KurlDatabase.Companion(createDatabaseDriver())

    @Single
    fun kurlStore(db: KurlDatabase): KurlStore = KurlStoreImpl(db)
}
