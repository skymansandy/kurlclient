package dev.skymansandy.kurlclient

import android.app.Application
import dev.skymansandy.kurlclient.di.initKoin

class KurlClientApp: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin(this)
    }
}
