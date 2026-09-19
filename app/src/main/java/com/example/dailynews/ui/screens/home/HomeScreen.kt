package com.oxclub.dailynews.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oxclub.dailynews.DailyNewsApp
import com.oxclub.dailynews.domain.model.Article
import com.oxclub.dailynews.ui.components.AdBanner
import com.oxclub.dailynews.ui.components.ArticleCard
import com.oxclub.dailynews.util.Resource
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen(
    category: String = "Top News",
    onArticleClick: (Article) -> Unit,
    vm: HomeViewModel = viewModel(
        factory = HomeViewModel.factory(
            (LocalContext.current.applicationContext as DailyNewsApp).repository
        )
    )
) {
    val articles by vm.articles.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(category) { vm.load(category) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category, style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = { vm.refresh(category) }) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }

        if (error != null) {
            Text(
                error!!,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.error
            )
        }

        AdBanner()

        if (loading && articles.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(articles, key = { it.url }) { article ->
                    ArticleCard(
                        article = article,
                        onClick = { onArticleClick(article) },
                        onBookmarkClick = { vm.toggleBookmark(article) }
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
                item { AdBanner() }
            }
        }
    }
}
