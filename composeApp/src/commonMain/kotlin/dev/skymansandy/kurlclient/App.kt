package dev.skymansandy.kurlclient

import androidx.compose.runtime.Composable
import dev.skymansandy.kurlclient.presentation.core.KurlAppScaffold
import dev.skymansandy.kurlclient.presentation.theme.KurlTheme

@Composable
fun App() {
    KurlTheme {
        KurlAppScaffold()
    }
}
