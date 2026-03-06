package dev.skymansandy.kurlclient

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform