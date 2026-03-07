package dev.skymansandy.kurlclient.di

import android.content.Context
import dev.skymansandy.kurl.store.initAndroidContext

fun initKoin(context: Context) {
    initAndroidContext(context)
    initKoin()
}
