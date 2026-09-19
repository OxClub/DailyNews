package com.oxclub.dailynews.ui.screens.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CategoriesScreen(onCategory: (String) -> Unit) {
    val categories = listOf(
        "Top News", "India", "Business", "Technology",
        "Sports", "Entertainment", "Science", "Health"
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Categories", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(categories) { category ->
                Card(
                    onClick = { onCategory(category) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                ) {
                    Text(category, modifier = Modifier.padding(18.dp))
                }
            }
        }
    }
}
