package com.oxclub.dailynews.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.oxclub.dailynews.DailyNewsApp
import com.oxclub.dailynews.domain.model.Article

private val Ink = Color(0xFF11243A)
private val Navy = Color(0xFF071A2E)
private val Coral = Color(0xFFFF6B4A)
private val Mist = Color(0xFFF5F7FA)
private val Muted = Color(0xFF718096)

@Composable
fun DailyNewsAppUi() {
    val app = LocalContext.current.applicationContext as DailyNewsApp
    val vm: NewsViewModel = viewModel(factory = NewsViewModel.factory(app.repository))
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Article?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    val articles by vm.articles.collectAsState()
    val trending by vm.trending.collectAsState()
    val saved by vm.saved.collectAsState()
    val results by vm.searchResults.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    MaterialTheme(colorScheme = lightColorScheme(primary = Ink, secondary = Coral, background = Mist)) {
        if (selected != null) ArticleScreen(selected!!, { selected = null }, { vm.toggleSaved(it) })
        else Scaffold(containerColor = Mist, bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                listOf("Home" to Icons.Default.Home, "Trending" to Icons.Default.Whatshot, "Topics" to Icons.Default.GridView, "Premium" to Icons.Default.AutoAwesome, "Saved" to Icons.Default.Bookmark).forEachIndexed { i, item ->
                    NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(item.second, null) }, label = { Text(item.first) })
                }
            }
        }) { padding ->
            when (tab) {
                0 -> HomeScreen(articles, loading, error, showSearch, results, vm, { showSearch = !showSearch }, { selected = it }, { vm.toggleSaved(it) }, Modifier.padding(padding))
                1 -> FeedScreen("Trending now", trending, false, null, { vm.loadTrending() }, { selected = it }, { vm.toggleSaved(it) }, Modifier.padding(padding), true)
                2 -> TopicsScreen({ vm.refresh(it); tab = 0 }, Modifier.padding(padding))
                3 -> PremiumScreen(Modifier.padding(padding))
                else -> FeedScreen("Saved stories", saved, false, null, {}, { selected = it }, { vm.toggleSaved(it) }, Modifier.padding(padding), false)
            }
        }
    }
}

@Composable private fun HomeScreen(items: List<Article>, loading: Boolean, error: String?, search: Boolean, results: List<Article>, vm: NewsViewModel, toggleSearch: () -> Unit, open: (Article) -> Unit, save: (Article) -> Unit, modifier: Modifier) {
    val categories = listOf("For you", "India", "World", "Business", "Technology", "Sports", "Health")
    var active by remember { mutableStateOf("For you") }
    val visible = if (search && results.isNotEmpty()) results else items
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { Header(toggleSearch) }
        if (search) item { SearchBar(vm) }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 18.dp)) {
                items(categories) { label -> FilterChip(selected = active == label, onClick = { active = label; if (label != "For you") vm.refresh(label) }, label = { Text(label) }) }
            }
        }
        if (error != null && items.isEmpty()) item { EmptyState(error, { vm.refresh() }) }
        if (loading && visible.isEmpty()) item { LoadingState() }
        if (visible.isNotEmpty()) {
            item { Text(if (search) "Search results" else "Top stories", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) }
            item { HeroCard(visible.first(), open, save) }
            itemsIndexed(visible.drop(1).take(12), key = { _, it -> it.url }) { _, article -> CompactCard(article, open, save) }
        }
    }
}

@Composable private fun Header(onSearch: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Navy).padding(start = 20.dp, end = 12.dp, top = 44.dp, bottom = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column { Text("DAILY", color = Coral, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black); Text("News", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold); Text("Stay curious. Stay informed.", color = Color(0xFFB5C2D0), style = MaterialTheme.typography.bodySmall) }
        IconButton(onClick = onSearch) { Icon(Icons.Default.Search, "Search", tint = Color.White) }
    }
}

@Composable private fun SearchBar(vm: NewsViewModel) { var query by remember { mutableStateOf("") }; OutlinedTextField(value = query, onValueChange = { query = it; vm.search(it) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), placeholder = { Text("Search headlines, topics or places") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true, shape = RoundedCornerShape(16.dp)) }

@Composable private fun HeroCard(a: Article, open: (Article) -> Unit, save: (Article) -> Unit) {
    Card(Modifier.padding(horizontal = 20.dp, vertical = 14.dp).fillMaxWidth().height(300.dp).clickable { open(a) }, shape = RoundedCornerShape(24.dp)) {
        Box {
            AsyncImage(model = a.urlToImage, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6001020)))))
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) { Text(a.sourceName.uppercase(), color = Coral, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold); Text(a.title, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(timeLabel(a.publishedAt), color = Color(0xFFD1D9E2), style = MaterialTheme.typography.labelSmall); IconButton(onClick = { save(a) }) { Icon(if (a.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, "Save", tint = Color.White) } } }
        }
    }
}

@Composable private fun CompactCard(a: Article, open: (Article) -> Unit, save: (Article) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).clickable { open(a) }, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        AsyncImage(model = a.urlToImage, contentDescription = null, modifier = Modifier.size(112.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE0E6ED)), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f)) { Text(a.sourceName, color = Coral, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold); Text(a.title, color = Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 3, overflow = TextOverflow.Ellipsis); Row(verticalAlignment = Alignment.CenterVertically) { Text(timeLabel(a.publishedAt), color = Muted, style = MaterialTheme.typography.labelSmall); Spacer(Modifier.weight(1f)); IconButton(onClick = { save(a) }, modifier = Modifier.size(28.dp)) { Icon(if (a.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, "Save", tint = Ink, modifier = Modifier.size(18.dp)) } } }
    }
}

@Composable private fun FeedScreen(title: String, items: List<Article>, loading: Boolean, error: String?, refresh: () -> Unit, open: (Article) -> Unit, save: (Article) -> Unit, modifier: Modifier, ranked: Boolean) { LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) { item { Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 52.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Column { Text(if (ranked) "THE PULSE" else "YOUR LIBRARY", color = Coral, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold); Text(title, color = Ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }; IconButton(onClick = refresh) { Icon(Icons.Default.Refresh, "Refresh") } } }; if (loading && items.isEmpty()) item { LoadingState() }; if (error != null && items.isEmpty()) item { EmptyState(error, refresh) }; itemsIndexed(items, key = { _, it -> it.url }) { index, a -> if (ranked) RankedCard(index + 1, a, open, save) else CompactCard(a, open, save) } } }

@Composable private fun RankedCard(rank: Int, a: Article, open: (Article) -> Unit, save: (Article) -> Unit) { Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp).clickable { open(a) }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Text("%02d".format(rank), color = Coral, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, modifier = Modifier.width(30.dp)); Column(Modifier.weight(1f)) { Text(a.sourceName, color = Muted, style = MaterialTheme.typography.labelSmall); Text(a.title, color = Ink, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis) }; AsyncImage(model = a.urlToImage, contentDescription = null, modifier = Modifier.size(74.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop) } }

@Composable private fun TopicsScreen(onTopic: (String) -> Unit, modifier: Modifier) { val topics = listOf("Top News" to Icons.Default.Public, "India" to Icons.Default.LocationOn, "World" to Icons.Default.Language, "Business" to Icons.Default.ShowChart, "Technology" to Icons.Default.Memory, "Sports" to Icons.Default.SportsSoccer, "Entertainment" to Icons.Default.Movie, "Science" to Icons.Default.Science, "Health" to Icons.Default.Favorite, "Politics" to Icons.Default.AccountBalance); LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 52.dp, 20.dp, 32.dp)) { item { Text("Explore", color = Coral, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold); Text("Topics for every curiosity", color = Ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Choose a beat and build your own front page.", color = Muted, modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)) }; items(topics.chunked(2)) { row -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) { row.forEach { (name, icon) -> Card(Modifier.weight(1f).padding(bottom = 12.dp).clickable { onTopic(name) }, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp)) { Icon(icon, null, tint = Coral); Spacer(Modifier.height(16.dp)); Text(name, color = Ink, fontWeight = FontWeight.Bold) } } }; if (row.size == 1) Spacer(Modifier.weight(1f)) } } } }

@Composable private fun PremiumScreen(modifier: Modifier) {
    Column(modifier.fillMaxSize().background(Navy).padding(24.dp), verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.AutoAwesome, null, tint = Coral, modifier = Modifier.size(44.dp))
        Text("DailyNews Premium", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp))
        Text("A calmer, deeper way to follow the world.", color = Color(0xFFB5C2D0), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf("Ad-free reading", "Personalized briefing", "Deep-dive topic digests", "Early access to new features").forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = Coral, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text(feature, color = Color.White, style = MaterialTheme.typography.bodyLarge) }
            }
        }
        Button(onClick = {}, modifier = Modifier.fillMaxWidth().padding(top = 34.dp), colors = ButtonDefaults.buttonColors(containerColor = Coral), shape = RoundedCornerShape(14.dp)) { Text("Coming soon", color = Color.White, fontWeight = FontWeight.Bold) }
        Text("Premium will unlock when subscriptions are connected.", color = Color(0xFF8FA1B4), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable private fun ArticleScreen(a: Article, back: () -> Unit, save: (Article) -> Unit) { val context = LocalContext.current; Scaffold(topBar = { TopAppBar(title = { Text("Story", color = Ink) }, navigationIcon = { IconButton(back) { Icon(Icons.Default.ArrowBack, "Back") } }, actions = { IconButton({ save(a) }) { Icon(if (a.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, "Save") } }) }) { p -> LazyColumn(Modifier.padding(p), contentPadding = PaddingValues(bottom = 32.dp)) { item { AsyncImage(model = a.urlToImage, contentDescription = null, modifier = Modifier.fillMaxWidth().height(230.dp), contentScale = ContentScale.Crop) }; item { Column(Modifier.padding(20.dp)) { Text(a.sourceName.uppercase(), color = Coral, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold); Text(a.title, color = Ink, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 10.dp)); Text(timeLabel(a.publishedAt), color = Muted, style = MaterialTheme.typography.labelSmall); Text(a.description ?: a.content ?: "Open the original publisher for the full story.", color = Color(0xFF39495A), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 22.dp)); Button({ context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(a.url))) }, modifier = Modifier.fillMaxWidth().padding(top = 24.dp), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.OpenInNew, null); Spacer(Modifier.width(8.dp)); Text("Read full story") } } } } } }

@Composable private fun LoadingState() { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Coral) } }
@Composable private fun EmptyState(message: String, retry: () -> Unit) { Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.CloudOff, null, tint = Muted, modifier = Modifier.size(42.dp)); Text(message, color = Muted, modifier = Modifier.padding(vertical = 12.dp)); TextButton(retry) { Text("Try again") } } }
private fun timeLabel(raw: String): String = raw.replace("T", " ").substringBefore(".").substringBefore("+").ifBlank { "Today" }
