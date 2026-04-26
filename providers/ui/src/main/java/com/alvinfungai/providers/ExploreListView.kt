package com.alvinfungai.providers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alvinfungai.providers.domain.model.ServiceProvider

@Composable
fun ExploreListView(
    providers: List<ServiceProvider>,
    onProviderClick: (String) -> Unit
) {
    // Sort providers by lastActiveAt descending (most recent first)
    val sortedProviders = remember(providers) {
        providers.sortedByDescending { it.lastActiveAt ?: 0L }
    }

    if (sortedProviders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No providers found", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            // Add extra bottom padding (80dp) to ensure the last item is always visible above the FAB
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = sortedProviders,
                key = { it.id ?: it.userId } // Fixed: key must be non-nullable Any
            ) { provider ->
                ServiceProviderCard(
                    provider = provider,
                    onClick = { provider.id?.let { onProviderClick(it) } }
                )
            }
        }
    }
}
