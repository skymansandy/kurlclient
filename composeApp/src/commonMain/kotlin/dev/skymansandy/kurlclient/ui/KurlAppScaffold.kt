package dev.skymansandy.kurlclient.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.skymansandy.kurlclient.ui.adaptive.WindowWidthClass
import dev.skymansandy.kurlclient.ui.adaptive.toWindowWidthClass
import dev.skymansandy.kurlclient.ui.request.RequestPanel
import dev.skymansandy.kurlclient.ui.response.ResponsePanel

private enum class NavDestination(val label: String) {
    New("New"), Collections("Collections"), History("History")
}

@Composable
fun KurlAppScaffold(vm: RequestViewModel = viewModel { RequestViewModel() }) {
    var selectedNav by remember { mutableStateOf(NavDestination.New) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        when (maxWidth.toWindowWidthClass()) {
            WindowWidthClass.Compact -> CompactScaffold(
                vm = vm,
                selectedNav = selectedNav,
                onNavSelect = { selectedNav = it }
            )
            else -> ExpandedScaffold(
                vm = vm,
                selectedNav = selectedNav,
                onNavSelect = { selectedNav = it }
            )
        }
    }
}

// ── Mobile layout ─────────────────────────────────────────────────────────────

@Composable
private fun CompactScaffold(
    vm: RequestViewModel,
    selectedNav: NavDestination,
    onNavSelect: (NavDestination) -> Unit
) {
    Scaffold(
        bottomBar = { KurlNavigationBar(selected = selectedNav, onSelect = onNavSelect) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedNav) {
                NavDestination.New -> Column(modifier = Modifier.fillMaxSize()) {
                    RequestPanel(
                        url = vm.url,
                        method = vm.method,
                        params = vm.params,
                        headers = vm.headers,
                        body = vm.body,
                        isLoading = vm.isLoading,
                        onUrlChange = vm::setRequestUrl,
                        onMethodChange = vm::setRequestMethod,
                        onParamUpdate = vm::updateParam,
                        onParamAdd = vm::addParam,
                        onParamRemove = vm::removeParam,
                        onHeaderUpdate = vm::updateHeader,
                        onHeaderAdd = vm::addHeader,
                        onHeaderRemove = vm::removeHeader,
                        onBodyChange = vm::setRequestBody,
                        onSend = vm::sendRequest,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                    HorizontalDivider()
                    ResponsePanel(
                        response = vm.response,
                        error = vm.error,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )
                }
                NavDestination.Collections -> CollectionsPlaceholder()
                NavDestination.History -> HistoryPlaceholder()
            }
        }
    }
}

// ── Desktop layout ────────────────────────────────────────────────────────────

@Composable
private fun ExpandedScaffold(
    vm: RequestViewModel,
    selectedNav: NavDestination,
    onNavSelect: (NavDestination) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        KurlNavigationRail(selected = selectedNav, onSelect = onNavSelect)
        VerticalDivider()

        when (selectedNav) {
            NavDestination.New -> Row(modifier = Modifier.fillMaxSize()) {
                RequestPanel(
                    url = vm.url,
                    method = vm.method,
                    params = vm.params,
                    headers = vm.headers,
                    body = vm.body,
                    isLoading = vm.isLoading,
                    onUrlChange = vm::setRequestUrl,
                    onMethodChange = vm::setRequestMethod,
                    onParamUpdate = vm::updateParam,
                    onParamAdd = vm::addParam,
                    onParamRemove = vm::removeParam,
                    onHeaderUpdate = vm::updateHeader,
                    onHeaderAdd = vm::addHeader,
                    onHeaderRemove = vm::removeHeader,
                    onBodyChange = vm::setRequestBody,
                    onSend = vm::sendRequest,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                VerticalDivider()
                ResponsePanel(
                    response = vm.response,
                    error = vm.error,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            NavDestination.Collections -> CollectionsPlaceholder()
            NavDestination.History -> HistoryPlaceholder()
        }
    }
}

// ── Navigation components ─────────────────────────────────────────────────────

@Composable
private fun KurlNavigationBar(
    selected: NavDestination,
    onSelect: (NavDestination) -> Unit
) {
    NavigationBar {
        NavDestination.entries.forEach { dest ->
            NavigationBarItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                icon = { NavIcon(dest) },
                label = { Text(dest.label) }
            )
        }
    }
}

@Composable
private fun KurlNavigationRail(
    selected: NavDestination,
    onSelect: (NavDestination) -> Unit
) {
    NavigationRail(modifier = Modifier.fillMaxHeight()) {
        NavDestination.entries.forEach { dest ->
            NavigationRailItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                icon = { NavIcon(dest) },
                label = { Text(dest.label) }
            )
        }
    }
}

@Composable
private fun NavIcon(dest: NavDestination) {
    when (dest) {
        NavDestination.New -> Icon(Icons.Default.Add, contentDescription = dest.label)
        NavDestination.Collections -> Icon(Icons.Default.List, contentDescription = dest.label)
        NavDestination.History -> Icon(Icons.Default.Search, contentDescription = dest.label)
    }
}

// ── Placeholder screens ───────────────────────────────────────────────────────

@Composable
private fun CollectionsPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Collections", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HistoryPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("History", style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}