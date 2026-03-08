package dev.skymansandy.kurl.store.di

import dev.skymansandy.kurl.store.CollectionRepository
import dev.skymansandy.kurl.store.CollectionStore
import dev.skymansandy.kurl.store.createDatabaseDriver
import dev.skymansandy.kurlstore.db.KurlDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class KurlStoreModule {

    @Single
    fun kurlDatabase(): KurlDatabase = KurlDatabase.Companion(createDatabaseDriver())

    @Single
    fun collectionStore(db: KurlDatabase): CollectionStore = CollectionRepository(db)
}