package dev.skymansandy.kurlclient.di

import android.content.Context
import dev.skymansandy.kurl.store.impl.data.db.initAndroidContext

fun initKoin(context: Context) {
    initAndroidContext(context)
    initKoin()
}
