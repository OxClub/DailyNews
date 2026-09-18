package com.example.dailynews.ui.screens.article

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dailynews.ui.components.AdBanner

@Composable
fun ArticleDetailScreen(encodedUrl: String) {
    val context = LocalContext.current
    val url = Uri.decode(encodedUrl)

    Column(Modifier.fillMaxSize()) {
        AdBanner()
        Column(Modifier.padding(20.dp)) {
            Text("News Article", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text(
                "This article is hosted by the original publisher. Open it to read the full story.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                try {
                    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
                } catch (_: Exception) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }) {
                Text("Read Full Article")
            }
        }
    }
}
