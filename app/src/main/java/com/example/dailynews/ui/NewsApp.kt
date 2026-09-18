@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.dailynews.ui

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.dailynews.BuildConfig
import com.example.dailynews.NewsViewModel
import com.example.dailynews.data.Article
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun NewsApp(vm: NewsViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Article?>(null) }
    var search by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Article>>(emptyList()) }

    if (selected != null) {
        ArticleReader(selected!!, onBack = { selected = null })
        return
    }

    Scaffold(
        topBar = {
            if (search) {
                SearchBar(
                    query = query,
                    onQuery = {
                        query = it
                        if (it.length >= 2) vm.search(it) { results = it }
                    },
                    onClose = { search = false; query = ""; results = emptyList() }
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text("DailyNews", fontWeight = FontWeight.Bold)
                            Text("India • World • Business • Tech", fontSize = 11.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = { search = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                        IconButton(onClick = { vm.refresh() }) {
                            Icon(Icons.Default.Refresh, "Refresh")
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(tab == 0, { tab = 0 }, { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(tab == 1, { tab = 1 }, { Icon(Icons.Default.Category, null) }, label = { Text("Categories") })
                NavigationBarItem(tab == 2, { tab = 2 }, { Icon(Icons.Default.Bookmark, null) }, label = { Text("Saved") })
            }
        }
    ) { padding ->
        when (tab) {
            0 -> Home(vm, padding, results.ifEmpty { vm.articles.collectAsState().value }, search, selected = { selected = it })
            1 -> Categories(vm, padding)
            2 -> Saved(vm, padding, selected = { selected = it })
        }
    }
}

@Composable
private fun Home(
    vm: NewsViewModel,
    padding: PaddingValues,
    articles: List<Article>,
    search: Boolean,
    selected: (Article) -> Unit
) {
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val category by vm.category.collectAsState()

    LazyColumn(
        modifier = Modifier.padding(padding).fillMaxSize(),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("general", "business", "entertainment", "health", "science", "sports", "technology")) { c ->
                    FilterChip(
                        selected = c == category,
                        onClick = { vm.refresh(c) },
                        label = { Text(c.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }

        item {
            AdBanner(Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
        }

        if (loading && articles.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(50.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        error?.let {
            item {
                Text(it, modifier = Modifier.padding(20.dp), color = MaterialTheme.colorScheme.error)
            }
        }

        if (!loading && articles.isEmpty() && error == null) {
            item {
                Text("No news available right now.", modifier = Modifier.padding(20.dp))
            }
        }

        if (articles.isNotEmpty()) {
            item {
                Text(
                    if (search) "Search results" else "Top Stories",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            item {
                FeaturedCard(articles.first(), vm, selected)
            }

            itemsIndexed(articles.drop(1)) { index, article ->
                if (index == 5) AdBanner(Modifier.padding(12.dp))
                CompactCard(article, vm, selected)
            }
        }
    }
}

@Composable
private fun FeaturedCard(article: Article, vm: NewsViewModel, selected: (Article) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).clickable { selected(article) },
        shape = RoundedCornerShape(18.dp)
    ) {
        Column {
            article.image?.let {
                AsyncImage(it, null, Modifier.fillMaxWidth().height(230.dp), contentScale = ContentScale.Crop)
            }
            Column(Modifier.padding(16.dp)) {
                Text(article.source, fontSize = 12.sp, color = Color(0xFF007A55), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(article.title, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                article.description?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, maxLines = 3, color = Color.Gray)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { vm.toggleSaved(article) }) {
                        Icon(
                            if (vm.isSaved(article)) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            "Save"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactCard(article: Article, vm: NewsViewModel, selected: (Article) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { selected(article) },
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        article.image?.let {
            AsyncImage(it, null, Modifier.size(112.dp, 82.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
        } ?: Spacer(Modifier.size(112.dp, 82.dp))
        Column(Modifier.weight(1f)) {
            Text(article.title, maxLines = 3, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(article.source, fontSize = 11.sp, color = Color.Gray)
        }
        IconButton(onClick = { vm.toggleSaved(article) }) {
            Icon(if (vm.isSaved(article)) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, "Save")
        }
    }
}

@Composable
private fun Categories(vm: NewsViewModel, padding: PaddingValues) {
    val categories = listOf(
        "general" to "Top News",
        "business" to "Business",
        "technology" to "Technology",
        "sports" to "Sports",
        "science" to "Science",
        "health" to "Health",
        "entertainment" to "Entertainment"
    )
    LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Categories", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(12.dp))
        }
        items(categories) { (id, name) ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { vm.refresh(id) },
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Article, null, tint = Color(0xFF007A55))
                    Spacer(Modifier.width(14.dp))
                    Text(name, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
        }
    }
}

@Composable
private fun Saved(vm: NewsViewModel, padding: PaddingValues, selected: (Article) -> Unit) {
    val saved by vm.saved.collectAsState()
    LazyColumn(Modifier.padding(padding).fillMaxSize()) {
        item {
            Text("Saved News", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(16.dp))
        }
        if (saved.isEmpty()) {
            item { Text("Articles you save will appear here.", modifier = Modifier.padding(16.dp), color = Color.Gray) }
        } else {
            items(saved) { CompactCard(it, vm, selected) }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQuery: (String) -> Unit, onClose: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, "Close search") }
        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search news") },
            singleLine = true
        )
    }
}

@Composable
private fun ArticleReader(article: Article, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(article.source, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            }
        )
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    loadUrl(article.url)
                }
            }
        )
    }
}

@Composable
private fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth().height(52.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BuildConfig.ADMOB_BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
