package com.example.dailynews.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.dailynews.ui.screens.article.ArticleDetailScreen
import com.example.dailynews.ui.screens.bookmarks.BookmarksScreen
import com.example.dailynews.ui.screens.categories.CategoriesScreen
import com.example.dailynews.ui.screens.home.HomeScreen
import com.example.dailynews.ui.screens.info.AboutScreen
import com.example.dailynews.ui.screens.info.PrivacyPolicyScreen
import com.example.dailynews.ui.screens.search.SearchScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    val showBottom = current in listOf(
        Screen.Home.route, Screen.Categories.route,
        Screen.Search.route, Screen.Bookmarks.route
    )

    Scaffold(
        bottomBar = {
            if (showBottom) {
                NavigationBar {
                    val items = listOf(
                        Triple(Screen.Home.route, "Home", Icons.Default.Home),
                        Triple(Screen.Categories.route, "Categories", Icons.Default.List),
                        Triple(Screen.Search.route, "Search", Icons.Default.Search),
                        Triple(Screen.Bookmarks.route, "Saved", Icons.Default.Bookmark)
                    )
                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = current == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(Screen.Home.route)
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(icon, null) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onArticleClick = { navController.navigate(Screen.Article.createRoute(it.url)) }
                )
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onCategory = { navController.navigate("${Screen.Home.route}?category=${android.net.Uri.encode(it)}") }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onArticleClick = { navController.navigate(Screen.Article.createRoute(it.url)) }
                )
            }
            composable(Screen.Bookmarks.route) {
                BookmarksScreen(
                    onArticleClick = { navController.navigate(Screen.Article.createRoute(it.url)) }
                )
            }
            composable(
                route = "article/{url}",
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { entry ->
                ArticleDetailScreen(
                    encodedUrl = entry.arguments?.getString("url").orEmpty()
                )
            }
            composable(Screen.About.route) { AboutScreen() }
            composable(Screen.Privacy.route) { PrivacyPolicyScreen() }
            composable(
                route = "home?category={category}",
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType; defaultValue = "Top News" }
                )
            ) {
                HomeScreen(
                    category = it.arguments?.getString("category") ?: "Top News",
                    onArticleClick = { navController.navigate(Screen.Article.createRoute(it.url)) }
                )
            }
        }
    }
}
