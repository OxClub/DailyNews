package com.example.dailynews.ui.screens.bookmarks

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
fun BookmarksScreen(
    onArticleClick: (Article) -> Unit,
    vm: BookmarksViewModel = viewModel(
        factory = BookmarksViewModel.factory(
            (LocalContext.current.applicationContext as DailyNewsApp).repository
        )
    )
) {
    val articles by vm.articles.collectAsState()

    Column {
        Text(
            "Saved Articles",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        AdBanner()
        if (articles.isEmpty()) {
            Text("No saved articles yet.", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn {
                items(articles, key = { it.url }) {
                    ArticleCard(
                        article = it,
                        onClick = { onArticleClick(it) },
                        onBookmarkClick = { vm.toggle(it) }
                    )
                }
            }
        }
    }
}
