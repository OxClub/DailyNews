@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.oxclub.dailynews.ui
import com.oxclub.dailynews.BuildConfig

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.oxclub.dailynews.NewsViewModel
import com.oxclub.dailynews.data.Article
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

private val Cream = Color(0xFFFFF8EF)
private val Peach = Color(0xFFFFE5D2)
private val Dark = Color(0xFF171717)
private val Gray = Color(0xFF777777)

@Composable
fun NewsApp(
    viewModel: NewsViewModel = viewModel()
) {
    val context = LocalContext.current

    val articles by viewModel.articles.collectAsState()
    val trending by viewModel.trending.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val trendingLoading by viewModel.trendingLoading.collectAsState()
    val loadingMore by viewModel.loadingMore.collectAsState()
    val error by viewModel.error.collectAsState()
    val category by viewModel.category.collectAsState()

    var tab by remember { mutableIntStateOf(0) }
    var readerArticle by remember { mutableStateOf<Article?>(null) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Article>>(emptyList()) }
    val signedIn = true
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }

    val drawerState =
        rememberDrawerState(DrawerValue.Closed)

    val scope =
        rememberCoroutineScope()

    val categories = listOf(
        "Top",
        "World",
        "India",
        "Business",
        "Technology",
        "Sports",
        "Entertainment",
        "Science",
        "Health"
    )

    if (readerArticle != null) {
        ArticleReader(
            article = readerArticle!!,
            onBack = {
                readerArticle = null
            }
        )
        return
    }

    val scheme =
        if (darkMode)
            darkColorScheme()
        else
            lightColorScheme()

    MaterialTheme(colorScheme = scheme) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    categories = categories,
                    signedIn = signedIn,
                    onSignIn = {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    },
                    onCategory = { name ->
                        scope.launch {
                            drawerState.close()
                        }

                        tab = 0

                        viewModel.refresh(
                            if (name == "Top")
                                "general"
                            else
                                name.lowercase()
                        )
                    },
                    onSaved = {
                        scope.launch {
                            drawerState.close()
                        }
                        tab = 3
                    },
                    onSettings = {
                        scope.launch {
                            drawerState.close()
                        }
                        tab = 4
                    }
                )
            }
        ) {
            Scaffold(
                containerColor =
                    if (darkMode)
                        MaterialTheme.colorScheme.background
                    else
                        Cream,
                topBar = {
                    Column {
                        SmallTopAppBar(
                            title = {
                                Text(
                                    "DailyNews",
                                    fontSize = 25.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Menu,
                                        "Menu"
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        searchOpen =
                                            !searchOpen
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        "Search"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        tab = 3
                                    }
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (saved.isNotEmpty()) {
                                                Badge {
                                                    Text(
                                                        if (saved.size > 99)
                                                            "99+"
                                                        else
                                                            saved.size.toString()
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            "Notifications"
                                        )
                                    }
                                }
                            },
                            colors =
                                TopAppBarDefaults
                                    .smallTopAppBarColors(
                                        containerColor =
                                            if (darkMode)
                                                MaterialTheme
                                                    .colorScheme
                                                    .surface
                                            else
                                                Peach
                                    )
                        )

                        if (searchOpen) {
                            SearchBar(
                                text = searchText,
                                onTextChange = {
                                    searchText = it

                                    if (it.length >= 2) {
                                        viewModel.search(it) {
                                            searchResults = it
                                        }
                                    } else {
                                        searchResults =
                                            emptyList()
                                    }
                                },
                                onClear = {
                                    searchText = ""
                                    searchResults =
                                        emptyList()
                                }
                            )
                        }

                        QuickActions()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(
                                    rememberScrollState()
                                )
                                .padding(
                                    horizontal = 12.dp
                                ),
                            horizontalArrangement =
                                Arrangement.spacedBy(22.dp)
                        ) {
                            categories.forEach { name ->
                                val active =
                                    (name == "Top" &&
                                        category == "general") ||
                                    name.lowercase() == category

                                Text(
                                    text = name,
                                    modifier = Modifier
                                        .clickable {
                                            tab = 0
                                            viewModel.refresh(
                                                if (name == "Top")
                                                    "general"
                                                else
                                                    name.lowercase()
                                            )
                                        }
                                        .padding(
                                            vertical = 10.dp
                                        ),
                                    fontWeight =
                                        if (active)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal,
                                    color =
                                        if (active)
                                            MaterialTheme
                                                .colorScheme
                                                .onBackground
                                        else
                                            Gray
                                )
                            }
                        }

                        HorizontalDivider()
                    }
                },
                bottomBar = {
                    NavigationBar {
                        BottomItem(
                            "Home",
                            Icons.Default.Home,
                            tab == 0
                        ) {
                            tab = 0
                        }

                        BottomItem(
                            "Trending",
                            Icons.Default.Whatshot,
                            tab == 1
                        ) {
                            tab = 1
                            viewModel.loadTrending()
                        }

                        BottomItem(
                            "Categories",
                            Icons.Default.GridView,
                            tab == 2
                        ) {
                            tab = 2
                        }

                        BottomItem(
                            "Saved",
                            Icons.Default.Bookmark,
                            tab == 3
                        ) {
                            tab = 3
                        }

                        BottomItem(
                            "Settings",
                            Icons.Default.Settings,
                            tab == 4
                        ) {
                            tab = 4
                        }
                    }
                }
            ) { padding ->

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {

                    if (tab != 4) {
                        AdBanner()
                    }

                    when (tab) {

                        0 -> {
                            val feed =
                                if (searchResults.isNotEmpty())
                                    searchResults
                                else
                                    articles

                            NewsFeed(
                                articles = feed,
                                loading = loading,
                                loadingMore = loadingMore,
                                error = error,
                                onRefresh = {
                                    viewModel.refresh()
                                },
                                onLoadMore = {
                                    viewModel.loadMore()
                                },
                                onOpen = {
                                    readerArticle = it
                                },
                                onSave = {
                                    viewModel.toggleSaved(it)
                                },
                                onDismiss = {
                                    viewModel.dismiss(it)
                                },
                                isSaved = {
                                    viewModel.isSaved(it)
                                },
                                onShare = {
                                    shareArticle(
                                        context,
                                        it
                                    )
                                }
                            )
                        }

                        1 -> {
                            NewsFeed(
                                articles = trending,
                                loading = trendingLoading,
                                loadingMore = loadingMore,
                                error = null,
                                onRefresh = {
                                    viewModel.loadTrending()
                                },
                                onLoadMore = {
                                    viewModel.loadMoreTrending()
                                },
                                onOpen = {
                                    readerArticle = it
                                },
                                onSave = {
                                    viewModel.toggleSaved(it)
                                },
                                onDismiss = {
                                    viewModel.dismiss(it)
                                },
                                isSaved = {
                                    viewModel.isSaved(it)
                                },
                                onShare = {
                                    shareArticle(
                                        context,
                                        it
                                    )
                                }
                            )
                        }

                        2 -> {
                            CategoriesScreen(
                                categories =
                                    categories.drop(1),
                                onCategory = {
                                    tab = 0
                                    viewModel.refresh(
                                        it.lowercase()
                                    )
                                }
                            )
                        }

                        3 -> {
                            SavedScreen(
                                articles = saved,
                                onOpen = {
                                    readerArticle = it
                                },
                                onSave = {
                                    viewModel.toggleSaved(it)
                                },
                                onShare = {
                                    shareArticle(
                                        context,
                                        it
                                    )
                                },
                                onClear = {
                                    viewModel.clearSaved()
                                }
                            )
                        }

                        4 -> {
                            SettingsScreen(
                                signedIn = signedIn,
                                notifications =
                                    notifications,
                                darkMode = darkMode,
                                onNotifications = {
                                    notifications = it
                                },
                                onDarkMode = {
                                    darkMode = it
                                },
                                onSignIn = {
                                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    categories: List<String>,
    signedIn: Boolean,
    onSignIn: () -> Unit,
    onCategory: (String) -> Unit,
    onSaved: () -> Unit,
    onSettings: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(Modifier.height(24.dp))

        Text(
            "DailyNews",
            modifier = Modifier.padding(
                horizontal = 24.dp
            ),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            if (signedIn)
                "Signed in"
            else
                "Your global news reader",
            modifier = Modifier.padding(
                horizontal = 24.dp,
                vertical = 4.dp
            ),
            color = Gray
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Icon(
                Icons.Default.AccountCircle,
                null
            )

            Spacer(Modifier.width(8.dp))

            Text(
                if (signedIn)
                    "Sign Out"
                else
                    "Sign In"
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(
                vertical = 14.dp
            )
        )

        Text(
            "NEWS",
            modifier = Modifier.padding(
                horizontal = 24.dp,
                vertical = 8.dp
            ),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Gray
        )

        categories.forEach { category ->
            NavigationDrawerItem(
                label = {
                    Text(category)
                },
                selected = false,
                onClick = {
                    onCategory(category)
                },
                icon = {
                    Icon(
                        Icons.Default.ChevronRight,
                        null
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(
                vertical = 12.dp
            )
        )

        NavigationDrawerItem(
            label = {
                Text("Bookmarks")
            },
            selected = false,
            onClick = onSaved,
            icon = {
                Icon(
                    Icons.Default.Bookmark,
                    null
                )
            },
            modifier = Modifier.padding(
                horizontal = 12.dp
            )
        )

        NavigationDrawerItem(
            label = {
                Text("Settings")
            },
            selected = false,
            onClick = onSettings,
            icon = {
                Icon(
                    Icons.Default.Settings,
                    null
                )
            },
            modifier = Modifier.padding(
                horizontal = 12.dp
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            "DailyNews • Version 1.0",
            modifier = Modifier.padding(24.dp),
            fontSize = 12.sp,
            color = Gray
        )
    }
}

@Composable
private fun SearchBar(
    text: String,
    onTextChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        singleLine = true,
        placeholder = {
            Text("Search worldwide news")
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                null
            )
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(
                    onClick = onClear
                ) {
                    Icon(
                        Icons.Default.Close,
                        null
                    )
                }
            }
        }
    )
}

@Composable
private fun QuickActions() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(18.dp)
    ) {
        QuickAction(
            Icons.Default.Star,
            "Premium",
            true
        )

        QuickAction(
            Icons.Default.SportsEsports,
            "Games",
            false
        )

        QuickAction(
            Icons.Default.TrendingUp,
            "Markets",
            true
        )

        QuickAction(
            Icons.Default.People,
            "Community",
            false
        )

        QuickAction(
            Icons.Default.MoreHoriz,
            "More",
            false
        )
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    title: String,
    new: Boolean
) {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(icon, null)
            }

            if (new) {
                Text(
                    "NEW",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(
                            RoundedCornerShape(6.dp)
                        )
                        .background(Color.Black)
                        .padding(
                            horizontal = 4.dp,
                            vertical = 1.dp
                        ),
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            title,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun NewsFeed(
    articles: List<Article>,
    loading: Boolean,
    loadingMore: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onOpen: (Article) -> Unit,
    onSave: (Article) -> Unit,
    onDismiss: (Article) -> Unit,
    isSaved: (Article) -> Boolean,
    onShare: (Article) -> Unit
) {
    if (loading && articles.isEmpty()) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {
            CircularProgressIndicator()

            Text(
                "Loading latest news…",
                modifier = Modifier.padding(
                    top = 90.dp
                ),
                color = Gray
            )
        }

        return
    }

    if (error != null && articles.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.Center
        ) {
            Icon(
                Icons.Default.CloudOff,
                null,
                modifier = Modifier.size(50.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Couldn't load the latest news",
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                error,
                color = Gray,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onRefresh
            ) {
                Icon(
                    Icons.Default.Refresh,
                    null
                )

                Spacer(Modifier.width(6.dp))

                Text("Retry")
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "If cached news is available, it will appear automatically.",
                color = Gray,
                fontSize = 11.sp
            )
        }

        return
    }

    if (articles.isEmpty()) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {
            Text("No news available right now.")
        }

        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 12.dp,
            vertical = 12.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Latest Stories",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Global news • Updated regularly",
                        color = Gray,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = onRefresh
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        "Refresh"
                    )
                }
            }
        }

        items(
            articles.distinctBy { it.url },
            key = { it.url }
        ) { article ->
            NewsCard(
                article = article,
                saved = isSaved(article),
                onOpen = {
                    onOpen(article)
                },
                onSave = {
                    onSave(article)
                },
                onDismiss = {
                    onDismiss(article)
                },
                onShare = {
                    onShare(article)
                }
            )
        }

        item {
            Button(
                onClick = onLoadMore,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 8.dp
                    ),
                enabled = !loadingMore
            ) {
                if (loadingMore) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    if (loadingMore)
                        "Loading…"
                    else
                        "Load more news"
                )
            }
        }
    }
}

@Composable
private fun NewsCard(
    article: Article,
    saved: Boolean,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column {

            if (!article.image.isNullOrBlank()) {
                AsyncImage(
                    model = article.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    contentScale =
                        androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        article.source.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Close,
                            "Dismiss"
                        )
                    }
                }

                Spacer(Modifier.height(5.dp))

                Text(
                    article.title,
                    fontSize = 19.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow =
                        TextOverflow.Ellipsis
                )

                if (!article.description.isNullOrBlank()) {
                    Spacer(Modifier.height(7.dp))

                    Text(
                        article.description!!,
                        color = Gray,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        article.publishedAt
                            .replace("T", " ")
                            .take(16),
                        color = Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onShare
                    ) {
                        Icon(
                            Icons.Default.Share,
                            "Share"
                        )
                    }

                    IconButton(
                        onClick = onSave
                    ) {
                        Icon(
                            if (saved)
                                Icons.Default.Bookmark
                            else
                                Icons.Default.BookmarkBorder,
                            "Save"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriesScreen(
    categories: List<String>,
    onCategory: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Categories",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Explore global stories by topic",
                color = Gray
            )

            Spacer(Modifier.height(8.dp))
        }

        items(categories) { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCategory(category)
                    },
                shape =
                    RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        20.dp
                    ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        null
                    )

                    Spacer(Modifier.width(12.dp))

                    Text(
                        category,
                        fontSize = 18.sp,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedScreen(
    articles: List<Article>,
    onOpen: (Article) -> Unit,
    onSave: (Article) -> Unit,
    onShare: (Article) -> Unit,
    onClear: () -> Unit
) {
    if (articles.isEmpty()) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.BookmarkBorder,
                    null,
                    modifier = Modifier.size(55.dp)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "No saved articles",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Tap the bookmark icon on any story.",
                    color = Gray
                )
            }
        }

        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(12.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    "Saved",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = onClear
                ) {
                    Text("Clear all")
                }
            }
        }

        items(
            articles,
            key = { it.url }
        ) { article ->
            NewsCard(
                article = article,
                saved = true,
                onOpen = {
                    onOpen(article)
                },
                onSave = {
                    onSave(article)
                },
                onDismiss = {},
                onShare = {
                    onShare(article)
                }
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    signedIn: Boolean,
    notifications: Boolean,
    darkMode: Boolean,
    onNotifications: (Boolean) -> Unit,
    onDarkMode: (Boolean) -> Unit,
    onSignIn: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(18.dp)
    ) {
        item {
            Text(
                "Settings",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Customize your DailyNews experience",
                color = Gray
            )

            Spacer(Modifier.height(18.dp))
        }

        item {
            SettingRow(
                "Notifications",
                "Breaking news and updates",
                notifications,
                onNotifications
            )
        }

        item {
            SettingRow(
                "Dark mode",
                "Use a darker appearance",
                darkMode,
                onDarkMode
            )
        }

        item {
            Spacer(Modifier.height(18.dp))

            Text(
                "ACCOUNT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Gray
            )

            ListItem(
                headlineContent = {
                    Text(
                        if (signedIn)
                            "Sign out"
                        else
                            "Sign in"
                    )
                },
                supportingContent = {
                    Text(
                        if (signedIn)
                            "You are signed in"
                        else
                            "Sign in to sync your news"
                    )
                },
                leadingContent = {
                    Icon(
                        Icons.Default.AccountCircle,
                        null
                    )
                },
                modifier = Modifier.clickable {
                    onSignIn()
                }
            )
        }

        item {
            ListItem(
                headlineContent = {
                    Text("About DailyNews")
                },
                supportingContent = {
                    Text("Global news reader • Version 1.0")
                },
                leadingContent = {
                    Icon(
                        Icons.Default.Info,
                        null
                    )
                }
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(title)
        },
        supportingContent = {
            Text(subtitle)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onChecked
            )
        }
    )
}

@Composable
private fun BottomItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = if (selected) MaterialTheme.colorScheme.primary else Gray
        )
        Text(
            title,
            fontSize = 11.sp,
            color = if (selected) MaterialTheme.colorScheme.primary else Gray
        )
    }
}
@Composable
private fun AdBanner() {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId =
                    BuildConfig.ADMOB_BANNER_ID

                loadAd(
                    AdRequest.Builder().build()
                )
            }
        }
    )
}

@Composable
private fun ArticleReader(
    article: Article,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    article.source,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        "Back"
                    )
                }
            }
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically =
                        true
                    webViewClient =
                        WebViewClient()

                    loadUrl(article.url)
                }
            }
        )
    }
}

private fun shareArticle(
    context: android.content.Context,
    article: Article
) {
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"

            putExtra(
                Intent.EXTRA_TEXT,
                "${article.title}\n\n${article.url}"
            )
        }

    context.startActivity(
        Intent.createChooser(
            intent,
            "Share article"
        )
    )
}

