package com.alvinfungai.providers

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTabs(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val categories = listOf("All", "Cleaning", "Repair", "Design", "Tech", "Construction", "Automotive", "Mobile", "Events", "Catering", "Education", "Training")
    
    // If the selected category is not in the predefined list (e.g., a specialty tag),
    // we display it as a selected chip alongside the others.
    val displayCategories = if (selectedCategory !in categories) {
        listOf(selectedCategory) + categories
    } else {
        categories
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            displayCategories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryChange(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = null // Modern cleaner look
                )
            }
        }

        // This is where the Map/List toggle button appears
        trailingIcon?.invoke()
    }
}