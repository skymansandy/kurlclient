package dev.skymansandy.kurlclient

import androidx.compose.runtime.Composable
import dev.skymansandy.kurlclient.ui.KurlAppScaffold
import dev.skymansandy.kurlclient.ui.theme.KurlTheme

@Composable
fun App() {
    KurlTheme {
        KurlAppScaffold()
    }
}