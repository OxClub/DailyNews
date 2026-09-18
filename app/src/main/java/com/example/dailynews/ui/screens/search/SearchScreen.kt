package com.example.dailynews.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailynews.DailyNewsApp
import com.example.dailynews.domain.model.Article
import com.example.dailynews.ui.components.AdBanner
import com.example.dailynews.ui.components.ArticleCard

@Composable
fun SearchScreen(
    onArticleClick: (Article) -> Unit,
    vm: SearchViewModel = viewModel(
        factory = SearchViewModel.factory(
            (LocalContext.current.applicationContext as DailyNewsApp).repository
        )
    )
) {
    var query by remember { mutableStateOf("") }
    val results by vm.results.collectAsState()
    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()

    Column {
        Text("Search", style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
            label = { Text("Search news") },
            trailingIcon = {
                TextButton(onClick = { vm.search(query) }) { Text("Search") }
            }
        )

        Spacer(Modifier.height(8.dp))
        AdBanner()

        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)) }

        LazyColumn {
            items(results, key = { it.url }) {
                ArticleCard(
                    article = it,
                    onClick = { onArticleClick(it) },
                    onBookmarkClick = { vm.toggle(it) }
                )
            }
        }
    }
}
